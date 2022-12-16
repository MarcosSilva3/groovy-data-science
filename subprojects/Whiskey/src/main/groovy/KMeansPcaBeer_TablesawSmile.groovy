/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import smile.clustering.KMeans
import smile.data.DataFrame
import smile.feature.transform.Standardizer
import smile.feature.extraction.PCA
import tech.tablesaw.api.*
import tech.tablesaw.plotly.api.*

def file = getClass().classLoader.getResource('beer.csv').file
def helper = new TablesawUtil(file)
def rows = Table.read().csv(file)

String[] cols = ['alcohol', 'calories', 'sodium', 'cost']
def df = DataFrame.of(rows.as().doubleMatrix(*cols), cols)
def scaler = Standardizer.fit(df)
def data = scaler.apply(df).toArray()

def dims = 4 // can be 2, 3 or 4
def pca = PCA.fit(data).getProjection(dims)
def projected = pca.apply(data)
def adj = [1, 1, 1, 5] // scaling factor to make graph pretty
def clusters = KMeans.fit(data, 3)
println clusters
def labels = clusters.y.collect { 'Cluster ' + (it + 1) }
rows = rows.addColumns(
    *(0..<dims).collect { idx ->
        DoubleColumn.create("PCA${idx+1}", (0..<df.size()).collect{
            adj[idx] * (projected[it][idx] + adj[idx])
        })
    },
    StringColumn.create('Cluster', labels)
)

def title = 'Clusters x Principal Components'
def type = dims == 2 ? ScatterPlot : Scatter3DPlot
//helper.show(type.create(title, rows, *cols, 'Cluster'), 'KMeansClustersPca')
helper.show(type.create(title, rows, *(1..dims).collect { "PCA$it" }, 'Cluster'), 'KMeansClustersPca')
