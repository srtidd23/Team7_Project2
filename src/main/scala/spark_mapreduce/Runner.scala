package spark_mapreduce

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Runner {

  val sparkEmoji: SparkEmoji = new SparkEmoji("local[4]")
  val twitterApi: TwitterApi = new TwitterApi(System.getenv("TWITTER_BEARER_TOKEN"))

  def main(args: Array[String]): Unit = {
    args match {
        //Returns a DataFrame containing the emojis separated from historic Twitter data
      case Array(func, path) if(func == "historic-emojis") =>  {
        sparkEmoji.uploadJSON(path, false, false)
        sparkEmoji.emojiValue(sparkEmoji.dfRaw).show()
      }
        //Returns a DataFrame containing the emojis separated from Twitter Stream data
      case Array(func, path, seconds) if(func == "stream-emojis") => {
        Future {
          twitterApi.sampleStreamToDir(debug=false)
        }
        sparkEmoji.uploadJSON(path, true, true)
        sparkEmoji.emojiValueStream(sparkEmoji.dfStreamRaw, seconds.toInt)
      }
      // Catch any other cases
      case _ => {
        printMenu()
        System.exit(-1)
      }
    }

  }
  def printMenu(): Unit ={
    println("________________________________________________USAGE_____________________________________________________________")
    println("historic-emojis <JSON path> | emojis info separated from historic Twitter data ")
    println("stream-emojis <JSON path> <seconds> | emojis info separated from Twitter Stream data")
  }

}
