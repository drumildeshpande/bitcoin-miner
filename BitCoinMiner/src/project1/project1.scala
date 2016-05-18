package project1
import java.security.MessageDigest
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import akka.io.Tcp
import akka.actor._
import akka.actor.Actor
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import akka.remote._


/* Constructors */
case class mineCoins(no_of_zeros: Integer)
case class mineCoinsRemote(no_of_zeros: Integer)
case class numMinedCoins(mined_bitcoins:ArrayBuffer[String])
case class doneMining(inputsprocessed:Integer)
case class assignTask(no_of_zeros:Integer)
/* -- Constructors -- */

/* Main */
object project1 extends App {
  var cmd_line_args:String=args(0)
  if(cmd_line_args.contains('.'))
  {
    println("Got ip as input, Invoking worker now")
    val worker =ActorSystem("workersystem").actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfInstances= 8)))
    for (n <- 1 to 8) {
      worker ! cmd_line_args
    }
  }
  else
  {
    println("In non IP input")
    var num_zeros = args(0).toInt
    val system = ActorSystem("master")
    val master = system.actorOf(Props[Master],name="Master")
    master ! assignTask(num_zeros)
    println("Invoking the master")
  }
}
/* -- Main -- */

/* Master Actor */
class Master extends Actor {
  private var num_zeros:Integer=0
  private var chk_workers:Integer=0
  private var num_inputs:Integer=0
  private var num_workers:Integer=0
  private var total_mined_coins:ArrayBuffer[String]=  ArrayBuffer[String]()

  def receive = {
    /* Assign work case: Invokes specified number of workers */
    case assignTask(x:Integer) => {
      num_zeros=x
      num_workers+=12
      println("Invoking the Local worker")
      val worker =context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfInstances= 12)))
      for (n <- 1 to 12) {
        worker ! mineCoins(num_zeros)
      }
    }

    /* Mined Coins case: Calculates total number of mined coins */
    case numMinedCoins(mined_bitcoins:ArrayBuffer[String]) => {
      total_mined_coins++=mined_bitcoins
      println("Worker reported number of mined coins")
    }

    /* Remote case: Invokes specified number of remote workers */
    case "remote" => {
      println("Invoking remote worker")
      num_workers+=8
      sender ! mineCoins(num_zeros)
    }

    /* Finished Mining case: Outputs found bitcoins */
    case doneMining(x:Integer) => {
      chk_workers+=1
      num_inputs+=x
      total_mined_coins=total_mined_coins.distinct
      for(i<- 0 until total_mined_coins.length )
        println((i+1)+" " + total_mined_coins(i))
      println("Total invoked workers (Local + Remote) : "+num_workers+" "+chk_workers)
      println("Total inputs processed : "+num_inputs)
      println("Number of bitcoins found : "+total_mined_coins.length)
      context.system.shutdown()
    }
  }
}
/* -- Master Actor -- */

/* Worker Actor */
class Worker extends Actor {
  def receive = {
    /* Start Mining case: Creates random string -> calculates it's hash -> Checks ot fpr leading zeroes -> returns result*/
    case mineCoins(num_zeros:Integer) => {
      println("Local worker -> number of zeros: "+num_zeros)
      var mined_coins:ArrayBuffer[String]=  ArrayBuffer[String]()
      val startingTime=System.currentTimeMillis()
      var inputs_processed:Integer=0
      var random1:String=Random.alphanumeric.take(6).mkString
      var random2:String=Random.alphanumeric.take(6).mkString

      while(System.currentTimeMillis()-startingTime<3600000){/*60000*/
        var s:String = "drumil723"+random1+random2+inputs_processed
        val sha_temp = MessageDigest.getInstance("SHA-256")
        var bitcoin:String=sha_temp.digest(s.getBytes).foldLeft("")((s:String, b: Byte) => s + Character.forDigit((b & 0xf0) >> 4, 16) +Character.forDigit(b & 0x0f, 16))
        var extracted_val:String=bitcoin.substring(0,num_zeros)
        var comparison_val="0"*num_zeros
        if(extracted_val.equals(comparison_val)){
          mined_coins+="drumil723"+random1+random2+inputs_processed+" "+bitcoin
        }
        inputs_processed+=1/*1000000*/
        if(inputs_processed%100000000==0) {
          println("Local worker reached Limit")
          if(mined_coins(0)!=null)
              println("The bitcoin found is: "+mined_coins(0))
          sender ! numMinedCoins(mined_coins)
        }
      }
      sender ! doneMining(inputs_processed)
    }

    /* Matches input pattern to IP address and then calls master */
    case ip:String => {
      println("Invoking remote worker")
      var port = 5152
      val master=context.actorSelection("akka.tcp://master@"+ip+":"+port+"/user/Master")
      master! "remote"
    }

    /* Mine Coins Remote: Remote bitcoin mining case */
    case mineCoinsRemote(num_zeros:Integer) => {
      println("In remote worker -> number of zeros: "+num_zeros)
      var mined_coins:ArrayBuffer[String]=  ArrayBuffer[String]()
      val startingTime=System.currentTimeMillis()
      var inputs_processed:Integer=0
      var random1:String=Random.alphanumeric.take(6).mkString
      var random2:String=Random.alphanumeric.take(6).mkString

      while(System.currentTimeMillis()-startingTime<60000){
        var s:String = "drumil723"+random1+random2+inputs_processed
        val sha_temp = MessageDigest.getInstance("SHA-256")
        var bitcoin:String=sha_temp.digest(s.getBytes).foldLeft("")((s:String, b: Byte) => s + Character.forDigit((b & 0xf0) >> 4, 16) +Character.forDigit(b & 0x0f, 16))
        var extracted_val:String=bitcoin.substring(0,num_zeros)
        var comparison_val="0"*num_zeros
        if(extracted_val.equals(comparison_val)){
          mined_coins+="drumil723"+random1+random2+inputs_processed+" "+bitcoin
        }
        inputs_processed+=1
        if(inputs_processed%1000000==0) {
          println("Remote worker reached limit")
          sender ! numMinedCoins(mined_coins)
        }
      }
      sender ! doneMining(inputs_processed)
    }
  }
}
/* -- Worker Actor -- */