package zio.workshop

import zio._
import zio.console._
import zio.stream._
import java.nio.{ ByteBuffer => JByteBuffer }
import zio.nio.core.SocketAddress
import zio.nio.core.channels.AsynchronousSocketChannel

object PokemonClient extends App {

  def run(args: List[String]): ZIO[Console, Nothing, Int] =
    open("0.0.0.0", 9002)
      .foldM(e => putStrLn("Error: " + e.getMessage) *> ZIO.succeed(1), _ => ZIO.succeed(0))

  case class Pokemon(name: String, hp: Int, attack: Int, defense: Int)

  object Decoder {
  
    val readCString: Chunk[Byte] => (String, Chunk[Byte]) = bytes => {
      val stringBytes = bytes.takeWhile(c => c != 0.toByte)
      val string = stringBytes.toArray.map(_.toChar).mkString
      (string, bytes.drop(stringBytes.length + 1))
    }
        
    val readInt: Chunk[Byte] => (Int, Chunk[Byte]) = bytes => {
      val (intBytes, rest) = bytes.splitAt(4)
      (JByteBuffer.wrap(intBytes.toArray).getInt, rest)
    }
    
  }

  object PokemonDecoder {
    
    /**
      * Write a function that will decode the chunk of bytes into an instance of `Pokemon` class.
      * For that purpose use the `Decoder` functions combined with `zio.Ref`.
      * 
      * Pokemon is encoded with the following bytes:
      * 1 byte  - char indicating an instance type. For a Pokemon it's always 'P'.
      * 4 bytes - integer indicating the length of the rest of Pokemon's bytes
      * cstring - nul terminated string of characters indicating the name
      * 4 bytes - integer indicating hp value
      * 4 bytes - integer indicating attack value
      * 4 bytes - integer indicating defense value
      *
      * @param bytes
      * @return
      */
    def fromBytes(bytes: Chunk[Byte]): UIO[Pokemon] = ???

  }

  /**
    * Write a function that will read a single Pokemon from a channel.
    *
    * @param channel
    * @return
    */
  def readPokemonHeader(channel: AsynchronousSocketChannel): IO[Exception, Pokemon] = 
    for {
      (id, len) <- channel.read(5).map { header => 
                     (header.toArray.head.toChar, 
                     JByteBuffer.wrap(header.toArray.drop(1).take(4)).getInt())
                   }
      rest      <- channel.read(len)
      p         <- PokemonDecoder.fromBytes(rest)
    } yield p

  /**
    * Write a function that will process a stream of n-Pokemons 
    * into some integer value (eg. calculate their total attack value)
    *
    * @param stream
    * @param num
    * @return
    */
  def processPokemons(stream: Stream[Nothing, Pokemon], n: Int): ZIO[Console, Nothing, Int] = ???

  /**
    * Write a function that will:
    * - open a connection to the server
    * - read the pokemons into a stream
    * - process the stream and print the result
    * 
    * First 4 bytes from the server's response is an integer 
    * indicating how many Pokemons will be send over the connection.
    * 
    * Use Managed, Queue, Stream and/*  */
    *
    * @param host
    * @param port
    * @return
    */
  def open(host: String, port: Int): ZIO[Console, Exception, Unit] = ???

}