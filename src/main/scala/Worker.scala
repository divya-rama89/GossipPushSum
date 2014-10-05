import akka.actor.Actor.Receive
import akka.actor._
import java.security.MessageDigest  
import Worker._
import scala.collection.mutable.ListBuffer
import scala.collection.TraversableOnce
import scala.util.control._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory

object Worker {
  
	case class PassMsg(msgBcast: String, tplgy: String,  workerNo:Int, numNodes:Int, actorsList: ArrayBuffer[ActorRef])
  case class setNeighbours(neighboursList: ArrayBuffer[ActorRef])
  case class getNeighbours()
  case class startGossiping(msg: String)
  case class hearGossiping(msg: String)
  case class doGossiping(msg: String)
  case class removeNode(act: ActorRef)
  /*
  def props(neighbourList: ArrayBuffer[ActorRef]):Props =
    Props(classOf[Worker], neighbourList)
  */
  def props(ac: ActorSystem, senderBoss: ActorRef): Props =
    Props(classOf[Worker], ac, senderBoss)
}

class Worker(ac: ActorSystem, superBoss: ActorRef) extends Actor {

  /*Added by Anirudh Subramanian Begin*/
  var neighboursList: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
  var visitedNeighboursList: ArrayBuffer[Int] = new ArrayBuffer[Int]
  var calledFirstTime: Boolean = true
  var cancellable:Cancellable = new Cancellable {override def isCancelled: Boolean = false

    override def cancel(): Boolean = false
  }
  var gossipHearCount = 0
  var gossipStartCount: Int = 0
  val root = ConfigFactory.load()
  val one  = root.getConfig("worker")
  var gossipTerminationLimit = one.getString("termination.gossipTermination").toInt

  /*Added by Anirudh Subramanian End*/

  def receive = {
    case PassMsg(msgBcast: String, tplgy: String, workerNo:Int, numNodes:Int, actorsList: ArrayBuffer[ActorRef]) => work(boss= sender, tplgy, msgBcast, workerNo, numNodes, actorsList)
    /*Added by Anirudh Subramanian for neighbour additi    case default => println("Received message "+default);
on begin*/
    case setNeighbours(neighboursList: ArrayBuffer[ActorRef]) => setNeighbours(neighboursList)
    /*Added by Anirudh Subramanian for neighbour addition End*/
    case getNeighbours() =>returnNeighbours (sender)
    case startGossiping(msg: String) => startGossip (msg)
    case hearGossiping(msg: String) => hearGossip(msg)
    case doGossiping(msg: String)   => doGossip(msg)
    case removeNode(act: ActorRef) => removeNode(act)
    case "tp" => println("this called")
  }

  private def removeNode(act: ActorRef): Unit = {
    neighboursList -= act
    visitedNeighboursList -= act.path.name.toInt
  }

  private def hearGossip(msg: String): Unit = {
    if(gossipStartCount == 0) {
      gossipStartCount += 1
      self ! startGossip(msg)

    } else {
      if ((gossipHearCount >= gossipTerminationLimit) && (visitedNeighboursList.size == neighboursList.size) ) {
        //println("Termination limit reached")
        //println("Cancel scheduling")
        //if(self.path.name.toInt != 0) {

        //}
        println("Inform super boss")
        for(i <- 0 to neighboursList.size - 1) {
          neighboursList(i) ! removeNode(self)
        }
        cancellable.cancel()
        println("name is " + self.path.name)
        superBoss ! gossipHeard(self.path.name)
        println("Kill yourself")
        self ! PoisonPill
      } else {
        //println("Inside receive of :: " + self.path.name)
        //println("Gossip is " + msg)
        println("still gossiping name is " + self.path.name)
        gossipHearCount = gossipHearCount + 1
        self ! doGossiping(msg)
      }
    }
  }

  private def startGossip(msg: String): Unit = {
    gossipStartCount += 1
    gossipHearCount += 1
    import ac.dispatcher
    cancellable = ac.scheduler.schedule(0 milliseconds, 100 milliseconds, self, doGossiping(msg))
  }

  private def doGossip(msg: String): Unit = {
    var rnd = new scala.util.Random()
    var x = rnd.nextInt(neighboursList.size)
    if(!visitedNeighboursList.contains(x)) {
      visitedNeighboursList += x
    }
    //println("Sending gossip to " + neighboursList(x).path.name)
    neighboursList(x) ! hearGossiping(msg)
  }

  /*Added by Anirudh Subramanian Begin*/

  def returnNeighbours (senderBoss: ActorRef): Unit = {
    senderBoss ! printTopologyNeighbours(neighboursList)
  }


  def setNeighbours(nList: ArrayBuffer[ActorRef]): Unit = {
    neighboursList = nList
  }
  /*Added by Anirudh Subramanian End*/

  def work(boss: ActorRef, tplgy: String, message: String, workerNo: Int, numNodes: Int, actorsList: ArrayBuffer[ActorRef]): Unit = {
  println("I #" + workerNo+" got created"+tplgy)
   
   //Anirudh, Do we need to wait until all workers configured or superboss takes care ???? From the code it looks like superboss sends msg only after worker has been created.
  
  /* Switch case :
  If line: pass to both side neighbours if not first or last node
  If full network: pass to all nodes
  If 2DGrid: pass to grid neighbours
  If Imperfect2DGrid: pass to  to grid neighbours and one random neighbour 
   */

  /*
  tplgy match{
    case "line" => linePass(workerNo, numNodes, message, actorsList)
    case "fullNetwork" => fnPass(workerNo, numNodes, message, actorsList)
    case "twoDGrid" => twoDGridPass(workerNo, numNodes, message, actorsList)
    case "imperfectTwoDGrid" => imperfectTwoDGridPass(workerNo, numNodes, message, actorsList)

    }
  }
  */
  /*
  def linePass(workerNo: Int, numNodes: Int, message: String, actorsList: ArrayBuffer[ActorRef]) = {
    println("case line") 
    if (workerNo == 0) 
    {
      //send message to node 1
      actorsList(1) ! message
      
    }
    else if (workerNo == numNodes-1 )
    {
      //send message to previous node only
      actorsList(workerNo-1) ! message
    }
    else
    {
      //send both neighbours
      actorsList(workerNo+1) ! message
      actorsList(workerNo-1) ! message
    }
  }
  
  def fnPass(workerNo: Int, numNodes: Int, message: String, actorsList: ArrayBuffer[ActorRef]) = {
    println("case fN")
    
    for (i<- 0 to numNodes-1) {
      if(i != workerNo) {
      actorsList(i) ! message
      }
    }
    }
  
   def twoDGridPass(workerNo: Int, numNodes: Int, message: String, actorsList: ArrayBuffer[ActorRef]) = {
    println("case 2DGrid")
    
    val x = (scala.math.sqrt(numNodes)).toInt
    
    // i<- 0 to numNodes-1
    
    //For the actors, is it better 
    
    // First row
    for (i<- 0 to x-1) {
      if(i == 0) {
      actorsList(1) ! message
      actorsList(x) ! message
      actorsList(x+1) ! message
      }
      else if(i == x-1) {
      actorsList(x-2) ! message
      actorsList(2*x) ! message
      actorsList((2*x)-1) ! message
      }
      else {
      actorsList(i-1) ! message
      actorsList(i+1) ! message
      actorsList(i+x-1) ! message
      actorsList(i+x) ! message
      actorsList(i+x+1) ! message
      }        
    }
    
    // Middle rows
    for (i<- x to (numNodes-x-1)) {
      // Left column
      if((i % x) == 0) {
      actorsList(i-x) ! message
      actorsList(i-x+1) ! message
      actorsList(i+1) ! message
      actorsList(i+x) ! message
      actorsList(i+x+1) ! message
      }
      // Right column
      else if((i % x) == 3) {
      actorsList(i-x) ! message
      actorsList(i-x-1) ! message
      actorsList(i-1) ! message
      actorsList(i+x-1) ! message
      actorsList(i+x) ! message
      }
      else {
      actorsList(i-x) ! message
      actorsList(i-x+1) ! message
      actorsList(i+1) ! message
      actorsList(i-x-1) ! message
      actorsList(i-1) ! message
      actorsList(i+x-1) ! message
      actorsList(i+x) ! message
      actorsList(i+x+1) ! message
      }        
    }
    
    // Last row
    for (i<- numNodes-x to numNodes-1) {
      if(i == numNodes-x) {
      actorsList(numNodes-(2*x)) ! message
      actorsList(numNodes-(2*x)+1) ! message
      actorsList(i+1) ! message
      }
      else if(i == numNodes-1) {
      actorsList(i-1) ! message
      actorsList(i-x) ! message
      actorsList((i-x)-1) ! message
      }
      else {
      actorsList(i-1) ! message
      actorsList(i+1) ! message
      actorsList(i-x-1) ! message
      actorsList(i-x) ! message
      actorsList(i-x+1) ! message
      }        
    }
    
    }
   
   def imperfectTwoDGridPass(workerNo: Int, numNodes: Int, message: String, actorsList: ArrayBuffer[ActorRef]) = {
    println("case imperfect2DGrid")
    
    val x = (scala.math.sqrt(numNodes)).toInt
    
    // i<- 0 to numNodes-1
    
    //For the actors, is it better 
    
    // First row
    for (i<- 0 to x-1) {
      if(i == 0) {
      actorsList(1) ! message
      actorsList(x) ! message
      actorsList(x+1) ! message
      }
      else if(i == x-1) {
      actorsList(x-2) ! message
      actorsList(2*x) ! message
      actorsList((2*x)-1) ! message
      }
      else {
      actorsList(i-1) ! message
      actorsList(i+1) ! message
      actorsList(i+x-1) ! message
      actorsList(i+x) ! message
      actorsList(i+x+1) ! message
      }        
    }
    
    // Middle rows
    for (i<- x to (numNodes-x-1)) {
      // Left column
      if((i % x) == 0) {
      actorsList(i-x) ! message
      actorsList(i-x+1) ! message
      actorsList(i+1) ! message
      actorsList(i+x) ! message
      actorsList(i+x+1) ! message
      }
      // Right column
      else if((i % x) == 3) {
      actorsList(i-x) ! message
      actorsList(i-x-1) ! message
      actorsList(i-1) ! message
      actorsList(i+x-1) ! message
      actorsList(i+x) ! message
      }
      else {
      actorsList(i-x) ! message
      actorsList(i-x+1) ! message
      actorsList(i+1) ! message
      actorsList(i-x-1) ! message
      actorsList(i-1) ! message
      actorsList(i+x-1) ! message
      actorsList(i+x) ! message
      actorsList(i+x+1) ! message
      }        
    }
    
    // Last row
    for (i<- numNodes-x to numNodes-1) {
      if(i == numNodes-x) {
      actorsList(numNodes-(2*x)) ! message
      actorsList(numNodes-(2*x)+1) ! message
      actorsList(i+1) ! message
      }
      else if(i == numNodes-1) {
      actorsList(i-1) ! message
      actorsList(i-x) ! message
      actorsList((i-x)-1) ! message
      }
      else {
      actorsList(i-1) ! message
      actorsList(i+1) ! message
      actorsList(i-x-1) ! message
      actorsList(i-x) ! message
      actorsList(i-x+1) ! message
      }        
    }

*/
   }

  }
  


  
  