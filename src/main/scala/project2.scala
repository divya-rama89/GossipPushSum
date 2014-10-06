import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.{blocking, future}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask
object project2 {
  def main(args: Array[String]): Unit = {

   // 3 args : numNodes, topology, algorithm
    
   // val numNodes: Option[Int] = toInt(args(0))
   // val topology: Option[Int] = toInt(args(1)) //convert to string later
   // val algorithm: Option[Int] = toInt(args(2)) //convert to string later
   
    //case class helloNodes(numberNodes: Int, msg: String)
    
    val numNodes = (args(0).toInt)
    val topology = (args(1))
    val algorithm = (args(2))  
    
    val two  = ConfigFactory.load().getConfig("super-boss")
    
    println("args are "+ numNodes+" "+topology+" "+algorithm);

      /*Modified by Anirudh Subramanian Begin*/
      val system = ActorSystem("super-boss", two)
      /*Modified by Anirudh Subramanian End*/

     
      val superbossservice = system.actorOf(SuperBoss.props(numNodes, system), "super-boss")
      println("path is " + superbossservice.path)
      superbossservice ! "STOP"
      implicit val timeout = Timeout(10 seconds)

      superbossservice ! setupNodes(numNodes, "Yo!", topology, algorithm)


       }

}
