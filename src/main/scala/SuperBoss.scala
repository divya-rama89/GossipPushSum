import akka.actor.{ActorRef, ActorSystem, Props, Actor, Inbox}
import SuperBoss._
import akka.util.Timeout
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.math
import Worker._
import scala.concurrent.{Future, blocking}
import akka.pattern.ask


case class setupNodes(numberNodes: Int, msg: String, top:String, algorithm: String)
case class printTopologyNeighbours(neigboursList: ArrayBuffer[ActorRef])
case class executeAlgo(algorithm: String)
case class gossipHeard(gossiper: String)
case class pushSumDone(gossiper: String, ratio: Double)
case class countNodes(gossiper: String)


object SuperBoss {
 
  def props(numberNodes: Int, ac: ActorSystem):Props =
    Props(classOf[SuperBoss], numberNodes, ac)
}

class SuperBoss(numberNodes: Int, ac: ActorSystem) extends Actor {
  //val workingActors: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
  var actorsList: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
  var msgBroadcast: String = ""
  var gossipersCompleted: ArrayBuffer[Int] = new ArrayBuffer[Int]
  var startTime: Long = 0
  
  def receive = {
    case setupNodes(numNodes: Int, msgBcast: String, tplgy: String, algorithm: String) => giveWork(sender(), numNodes, msgBcast, tplgy, algorithm)
    case printTopologyNeighbours(neighboursList: ArrayBuffer[ActorRef]) => printTopologyNeighbours(sender, neighboursList)
    case executeAlgo(algorithm: String) => executeAlgo(algorithm: String)
    case gossipHeard(gossiper: String) => recordGossipingNode(sender, gossiper)
    case countNodes(gossiper: String) => countNodes(gossiper)
    case pushSumDone(gossiper: String, ratio: Double) => pushSumDone(gossiper, ratio)
    case "STOP" => println("hi"); //shutDownBoss()
  }

    private def countNodes(gossiper: String): Unit = {
      if(!gossipersCompleted.contains(gossiper.toInt)) {
        // Commented by DR         
        // println("Gossip heard by " + gossiper)
        gossipersCompleted += gossiper.toInt
      }
      if(gossipersCompleted.size == numberNodes) {
        println("===============================================")
        println("Gossip Completed Once for all Nodes")
        println("===============================================")
        
       // println("Time taken for running algo is "+ (System.currentTimeMillis()-startTime).toString)
      }
    }

    private def pushSumDone(gossiper: String, ratio: Double): Unit = {
      println("Pushsum submitted by " + gossiper)
      println("Pushsum converged")
      println("Convergence time is  blah blah")
      println("Converged ratio is " + ratio)
      context.stop(self)
    }


    private def recordGossipingNode(senderWorker: ActorRef, gossiper: String): Unit = {
      /*
      if(!gossipersCompleted.contains(gossiper.toInt)) {
        println("Gossip heard by " + gossiper)
        gossipersCompleted += gossiper.toInt
      }
      */
      //gossipersCompleted += gossiper.toInt
      println("Gossip heard by " + gossiper)
      //if (gossipersCompleted.size >= numberNodes - 1) {
      println("Gossip heard by all")
      println("Convergence time is "+ (System.currentTimeMillis()-startTime).toString+" milliseconds.")
      //println("Convergence time is blah blah")
      context.stop(self)
      //}
    }

    override def postStop() = {
      println("=========================================================")
      println("All workers and boss shut down")
      println("=========================================================")
      ac.shutdown()
    }

    private def executeAlgo(algorithm: String): Unit = {
      
      startTime = System.currentTimeMillis();
      
      algorithm match {
        case "gossip" => {
           actorsList(0) ! startGossiping(msgBroadcast)
        }

        case "push-sum" => {
          actorsList(0) ! startPushSumCalculation()
        }

        case default => println("Invalid algo:"+default)
      }
    }

    private def setNeighbours2D(neighbourList: Array[Array[ActorRef]], actorsList: ArrayBuffer[ActorRef], algorithm: String) {
      var noOfRows    = neighbourList.size
      var noOfColumns = neighbourList(0).size
      var includeTop: Boolean = true
      var includeLeft: Boolean = true
      var includeRight: Boolean = true
      var includeBottom: Boolean = true

      for(i <- 0 to noOfRows - 1) {
        for(j <- 0 to noOfColumns - 1) {
          var neighbours: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
          if(i == 0) {
            includeTop = false
          }
          if(j == 0) {
            includeLeft = false
          }
          if(i == (noOfRows - 1) ) {
            includeBottom = false
          }
          if(j == (noOfColumns - 1)) {
            includeRight = false
          }

          if(includeTop) {
            neighbours += neighbourList(i - 1)(j)
          }

          if(includeLeft) {
            neighbours += neighbourList(i)(j - 1)
          }

          if(includeBottom) {
            neighbours += neighbourList(i + 1)(j)
          }

          if(includeRight) {
            neighbours += neighbourList(i)(j + 1)
          }



          actorsList(noOfColumns*i + j) ! setNeighbours(neighbours)

          includeBottom = true
          includeTop    = true
          includeLeft   = true
          includeRight  = true
        }
      }

    }

    private def chooseOtherRandomNeighbour(xPosition: Int, yPosition: Int, totalRows: Int, totalColumns: Int): List[Int] = {
      var rnd = new scala.util.Random()
      var x = rnd.nextInt(totalRows)
      while(xPosition == x) {
        x = rnd.nextInt(totalRows)
      }
      var y = rnd.nextInt(totalRows)
      while(yPosition == y) {
        y = rnd.nextInt(totalColumns)
      }

      return List(x, y)
    }

    private def setRandomNeighbours2D(neighbourList: Array[Array[ActorRef]], actorsList: ArrayBuffer[ActorRef], algorithm: String) {
      var noOfRows    = neighbourList.size
      var noOfColumns = neighbourList(0).size
      var includeTop: Boolean = true
      var includeLeft: Boolean = true
      var includeRight: Boolean = true
      var includeBottom: Boolean = true

      for(i <- 0 to noOfRows - 1) {
        for(j <- 0 to noOfColumns - 1) {
          var neighbours: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
          if(i == 0) {
            includeTop = false
          }
          if(j == 0) {
            includeLeft = false
          }
          if(i == (noOfColumns - 1) ) {
            includeBottom = false
          }
          if(j == (noOfRows - 1)) {
            includeRight = false
          }

          if(includeTop) {
            neighbours += neighbourList(i - 1)(j)
          }

          if(includeLeft) {
            neighbours += neighbourList(i)(j - 1)
          }

          if(includeBottom) {
            neighbours += neighbourList(i + 1)(j)
          }

          if(includeRight) {
            neighbours += neighbourList(i)(j + 1)
          }

          var randomNeighbour:List[Int] = chooseOtherRandomNeighbour(i, j, noOfRows, noOfColumns)
          neighbours += neighbourList(randomNeighbour(0))(randomNeighbour(1))

          actorsList(noOfColumns*i + j) ! setNeighbours(neighbours)

          includeBottom = true
          includeTop    = true
          includeLeft   = true
          includeRight  = true
        }
      }

    }

    private def printTopology(actorsList: ArrayBuffer[ActorRef], algorithm: String): Unit = {
        blocking {
          for (i <- 0 to actorsList.size - 1) {
            actorsList(i) ! getNeighbours()
          }
        }
        self ! executeAlgo(algorithm)

    }

    private def printTopologyNeighbours(senderActor: ActorRef, neighbourList: ArrayBuffer[ActorRef]): Unit = {
      print("Actor name is " + senderActor.path.name + ":::")
      print("Neighbours " )
      for (i <-0 to neighbourList.size - 1) {
        print(neighbourList(i).path.name)
        if(i != neighbourList.size - 1) {
          print("   ")
        }  else {
          println()
        }

      }

    }



    private def giveWork(senderBoss: ActorRef, numNodes: Int, msgBcast: String, tplgy: String, algorithm: String): Future[Int] = {
   
    //var actorsList: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
    msgBroadcast = msgBcast

    var newNumNodes = 0
    var i = 0

    var updatedNumNodes: Int = numNodes
    if (tplgy == "2D" || tplgy == "imp2D") {
      val x = scala.math.ceil(scala.math.sqrt(numNodes)).toInt
      updatedNumNodes = x*x
    }

    for (i <- 0 to updatedNumNodes - 1) {
      val r: ActorRef = context.actorOf(
        Worker.props(ac, self), i.toString()
      )
      actorsList +=r
    }

    tplgy match{
      /*Commented by Anirudh Subramanian Begin*/
      /*
      case "line" =>
      {
        newNumNodes = numNodes
      }
      */
      /*Commented by Anirudh Subramanian End*/
      /*Added by Anirudh Subramanian Begin*/
      case "line" => {
        /*Divya Code Refactor Begin*/
        for (i <- 0 to numNodes - 1) {
          var neighbourListLocal: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
          if(i == 0) {
            neighbourListLocal += actorsList(i + 1)
          }
          else if(i == (numNodes - 1)) {
            neighbourListLocal += actorsList(i - 1)
          }
          else {
            neighbourListLocal += actorsList(i - 1)
            neighbourListLocal += actorsList(i + 1)
          }

          actorsList(i) ! setNeighbours(neighbourListLocal)


        }
        /*Divya Code Refactor End*/
        printTopology(actorsList, algorithm)


      }
      /*Added by Anirudh Subramanian End*/
      /*Added by Anirudh Subramanian Begin*/
      case "full" =>
      {
        /*Divya Code Refactor Begin*/
        for (i <- 0 to numNodes - 1) {
          var neighbourListLocal: ArrayBuffer[ActorRef] = new ArrayBuffer[ActorRef]
          for(j <- 0 to numNodes - 1) {
            if(i != j) {
              neighbourListLocal += actorsList(j)
            }
          }
          actorsList(i) ! setNeighbours(neighbourListLocal)

        }
        /*Divya Code Refactor End*/
        printTopology(actorsList, algorithm)


      }
      /*Added by Anirudh Subramanian End*/
      /*Added by Anirudh Subramanian Begin*/
      case "2D" =>
      {
        val x = scala.math.floor(scala.math.sqrt(updatedNumNodes)).toInt

        var neighbourlistLocal = Array.ofDim[ActorRef](x, x)
        println("Updated nodes are " + x)
        for (i <- 0 to (x - 1)) {
          for (j <- 0 to (x - 1)) {
            println(i*x + j)
            neighbourlistLocal(i)(j) = actorsList(i*(x) + j)
          }
        }

        setNeighbours2D(neighbourlistLocal, actorsList, algorithm)

        printTopology(actorsList, algorithm)

        //executeAlgo(algorithm)
      }
      /*Added by Anirudh Subramanian End*/
      /*Added by Anirudh Subramanian Begin*/
      case "imp2D" =>
      {
        val x = scala.math.floor(scala.math.sqrt(updatedNumNodes)).toInt

        var neighbourlistLocal = Array.ofDim[ActorRef](x, x)
        println("Updated nodes are " + x)
        for (i <- 0 to (x - 1)) {
          for (j <- 0 to (x - 1)) {
            println(i*x + j)
            neighbourlistLocal(i)(j) = actorsList(i*(x) + j)
          }
        }
        setRandomNeighbours2D(neighbourlistLocal, actorsList, algorithm)

        printTopology(actorsList, algorithm)

        //executeAlgo(algorithm)
      }
    case default => println("Invalid topology:"+default)
    }

    println("=====================================================")
    //executeAlgo(algorithm)
    println("Number of nodes = "+numNodes)
    Future(1 + 2)
     /*Commented by Anirudh Subramanian*/
    /*
    for (i<-0 to newNumNodes-1) {
      val r: ActorRef = context.actorOf(
        Props[Worker], i.toString()
      )
      actorsList += r
    }
    */
    /*Commented by Anirudh Subramanian*/
    /*
    for (i<- 0 to newNumNodes-1) {
      actorsList(i) ! PassMsg(msgBcast, tplgy, i, newNumNodes, actorsList)
      workingActors += actorsList(i)
    }
    */
 
    }
    
}
