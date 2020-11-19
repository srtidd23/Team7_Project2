package spark_mapreduce

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Runner {

  val sparkEmoji: SparkEmoji = new SparkEmoji("local[4]")
  val twitterApi: TwitterApi = new TwitterApi(System.getenv("TWITTER_BEARER_TOKEN"))

  def main(args: Array[String]): Unit = {
    args match {
        //TODO delete : Returns a DataFrame containing the emojis separated from historic Twitter data
      case Array(func, path) if(func == "historic-emojis") =>  {
        sparkEmoji.uploadJSON(path, false, false)
        sparkEmoji.emojiValue(sparkEmoji.dfRaw).show()
      }
        //TODO delete : Returns a DataFrame containing the emojis separated from Twitter Stream data
      case Array(func, path, seconds) if(func == "stream-emojis") => {
        Future {
          twitterApi.sampleStreamToDir("tweet.fields=public_metrics,created_at,lang&user.fields=public_metrics&expansions=author_id",debug=false)
        }
        sparkEmoji.uploadJSON(path, true, true)
        sparkEmoji.emojiValueStream(sparkEmoji.dfStreamRaw)
      }

      //Question 3
      case Array(func, path, lang1, lang2) if(func == "language-top-emojis") =>{
        sparkEmoji.uploadJSON(path, false, false)
        sparkEmoji.langTopEmojisHist(sparkEmoji.dfRaw, lang1, lang2)
      }

      //Question 4
      case Array(func, path, like) if(func == "popular-tweet-emojis") =>{
        sparkEmoji.uploadJSON(path, false, false)
        sparkEmoji.popTweetsEmojiHist(sparkEmoji.dfRaw, like.toLowerCase.toBoolean)
      }

        //Question 5
      case Array(func, path, threshold, seconds) if(func == "popular-people-emojis") =>{
        Future {
          twitterApi.sampleStreamToDir("tweet.fields=public_metrics,created_at,lang&user.fields=public_metrics&expansions=author_id",debug=false)
        }
        sparkEmoji.uploadJSON(path, false, true)
        sparkEmoji.popPeepsEmojisStream(sparkEmoji.emojiValueStream(sparkEmoji.dfStreamRaw), threshold.toInt, seconds.toInt)
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
    println("language-top-emojis <JSON path> <first language> <second language> <seconds> | top emojis in first language with how many are used in second language")
    println("popular-tweet-emojis <JSON path> <like boolean> | most liked or retweeted emojis")
    println("popular-people-emojis <JSON path> <followers minimum> <seconds> | most popular emojis among famous people")
  }

}
