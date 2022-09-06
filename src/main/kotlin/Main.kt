import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.random.Random

suspend fun main() {
     val br: BufferedReader = File("config.txt").inputStream().bufferedReader()
     val rCount = br.readLine().toInt()
     val wCount = br.readLine().toInt()
     val idList = br.readLine().split(",").toTypedArray()

     val client = HttpClient.newBuilder().build()
     val createAccountsJobs = listOf (
          idList.forEach {
               GlobalScope.launch() {
                    val values = mapOf("id" to it)

                    val requestBody: String = ObjectMapper().writeValueAsString(values)
                    val request = HttpRequest.newBuilder()
                         .uri(URI.create("http://localhost:8080"))
                         .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                         .header("Content-Type", "application/json")
                         .build()
                    client.send(request, HttpResponse.BodyHandlers.ofString())
               }
          })


     val getAmountJobs = List(rCount) {
          GlobalScope.launch (start = CoroutineStart.LAZY){
               val randomIndex = Random.nextInt(idList.size)
               val request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080?id=" + idList[randomIndex]))
                    .build()
               val response = client.send(request, HttpResponse.BodyHandlers.ofString())
               println(response.body())
          }
     }
     val addAmountJobs = List(wCount) {
          GlobalScope.launch (start = CoroutineStart.LAZY){
               val values = mapOf("id" to idList[Random.nextInt(idList.size)], "amount" to Random.nextInt(100) - 50)

               val requestBody: String = ObjectMapper().writeValueAsString(values)
               val request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080"))
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "application/json")
                    .build()
               client.send(request, HttpResponse.BodyHandlers.ofString())
          }
     }
     val allJobs = addAmountJobs + getAmountJobs
     allJobs.forEach { it.start() }
     allJobs.joinAll()
}