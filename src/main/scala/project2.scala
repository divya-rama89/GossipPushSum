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
    
    // val output: Int = x.getOrElse(-1)
    
   // val root = ConfigFactory.load()
   // val one  = root.getConfig("superBossSystem")
   // val two  = root.getConfig("bossSystem")
    
    println("args are "+ numNodes+" "+topology+" "+algorithm);

      /*Modified by Anirudh Subramanian Begin*/
      val system = ActorSystem("super-boss")
      /*Modified by Anirudh Subramanian End*/

     
      val superbossservice = system.actorOf(SuperBoss.props(numNodes, system), "super-boss")
      println("path is " + superbossservice.path)
      superbossservice ! "STOP"
      implicit val timeout = Timeout(10 seconds)

      superbossservice ! setupNodes(numNodes, "Yo!", topology, algorithm)


      //superbossservice ! executeAlgo(algorithm)

      //val serverbossservice = system.actorOf(MinerBoss.props(system))
//      import system.dispatcher
//      system.scheduler.scheduleOnce(120000 milliseconds, superbossservice, "STOP")
      
      // Making the server work
//      val bossservice = system.actorOf(MinerBoss.props(system)) 
//      bossservice ! connectTo("super-boss", one.getString("akka.remote.netty.tcp.hostname"))
      
          
  
  /*    
      //remoting code here
      /*
      val system = ActorSystem("boss-system", ConfigFactory.parseString("""
    akka {
       actor {
          provider = "akka.remote.RemoteActorRefProvider"
             }
       remote {
           transport = ["akka.remote.netty.tcp"]
       netty.tcp {
           hostname = "127.0.0.1"
           port = 2259
                 }
             }
        }
                                                                        """))
      */
      
      /*Modified by   Anirudh Subramanian Begin*/
      val system = ActorSystem("boss-system", two)
      /*Modified by Anirudh Subramanian End*/
      val bossservice = system.actorOf(MinerBoss.props(system))
     
      bossservice ! connectTo("super-boss", args(0))

  
    /*
    val system = ActorSystem("miner-system")
    val minerService =
      system.actorOf(props(system), "minerboss")
    */
 */
  }

  /*
  def toInt(s: String):Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e:Exception => None
    }
  } */
}