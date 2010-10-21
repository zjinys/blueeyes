package blueeyes.core.http

import scala.util.matching.Regex
import blueeyes.util.ProductPrefixUnmangler
/*
Usage: 

import MimeTypes._
val mimeType = image/gif
val imageMimeType = image/gif 
val javaScriptMimeType = text/javascript
*/

sealed trait MimeType {
  def maintype: String
  def subtype: String
  
  def value: String = maintype + "/" + subtype
  
  def extensions: List[String] = subtype :: Nil
  
  def defaultExtension = extensions.head
}

object MimeTypes {

  def parseMimeTypes(inString: String): Array[MimeType] = {
    def MimeTypeRegex = new Regex("""([a-z\-]+)/([.+a-z\-]+)""")

    /* Split the string on commas, which separate the mimes */
    var outMimes: Array[MimeType] = inString.toLowerCase.split(",").map(_.trim)
      .flatMap(MimeTypeRegex findFirstIn _).map(_.split("/"))
      .flatMap ( mimeType =>  mimeType match {
        case Array("application" , "javascript") => Array(application / javascript)
        case _ => Nil
      }
    )
    return outMimes 
  }

  trait GenericType extends ProductPrefixUnmangler {
    def subtype = unmangledName
  }

  sealed abstract class ApplicationType(val extensions: List[String]) extends GenericType
  sealed abstract class AudioType(val extensions: List[String]) extends GenericType
  sealed abstract class ImageType(val extensions: List[String]) extends GenericType
  sealed abstract class MessageType(val extensions: List[String]) extends GenericType
  sealed abstract class MultipartType(val extensions: List[String]) extends GenericType
  sealed abstract class PrsType(val extensions: List[String]) extends GenericType
  sealed abstract class TextType(val extensions: List[String]) extends GenericType
  sealed abstract class VideoType(val extensions: List[String]) extends GenericType

  /* Application Types */
  sealed abstract class JavaScriptApplicationType extends ApplicationType("js" :: Nil)
  sealed abstract class OggApplicationType extends ApplicationType("ogg" :: Nil)

  case object javascript extends JavaScriptApplicationType
  case object `x-javascript` extends JavaScriptApplicationType
  case object `soap+xml` extends ApplicationType("soap+xml" :: Nil)
  case object `xhtml+xml` extends ApplicationType("xhtml+xml" :: Nil)
  case object `xml-dtd` extends ApplicationType("xml-dtd" :: Nil)

  case object json extends ApplicationType("json" :: Nil)
  case object `x-latex` extends ApplicationType("latex" :: Nil)
  case object `octect-stream` extends ApplicationType("bin" :: "class" :: "dms" :: "exe" :: "lha" :: "lzh" :: Nil)
  case object ogg extends JavaScriptApplicationType
  case object pdf extends ApplicationType("pdf" :: Nil)
  case object postscript extends ApplicationType("ai" :: Nil)
  case object `x-dvi` extends ApplicationType("dvi" :: Nil)
  case object `x-shockwave-flash` extends ApplicationType("swf" :: Nil)
  case object `x-tar` extends ApplicationType("tar" :: Nil)
  case object `x-ttf` extends ApplicationType("ttf" :: Nil)
  case object zip extends ApplicationType("zip" :: Nil)

  /* Audio Types */
  sealed abstract class MpegAudioType extends AudioType("mpg" :: "mpeg" :: "mpga" :: "mpe" :: "mp3" :: "mp2" :: Nil)
  sealed abstract class Mp4AudioType extends AudioType("mp4" :: Nil)

  case object basic extends AudioType("au" :: "snd" :: Nil)
  case object mp4 extends Mp4AudioType
  case object midi extends AudioType("midi" :: "mid" :: "kar" :: Nil)
  case object mpeg extends MpegAudioType
  case object vorbis extends AudioType("vorbis" :: Nil)
  case object `x-ms-wma` extends AudioType("wma" :: Nil)
  case object `x-ms-wax` extends AudioType("wax" :: Nil)
  case object `x-realaudio` extends AudioType("ra" :: Nil)
  case object `x-wav` extends AudioType("wav" :: Nil)

  /* Image Types */ 
  case object gif extends ImageType("gif" :: Nil)
  case object png extends ImageType("png" :: Nil)
  case object jpeg extends ImageType("jpg" :: "jpeg" :: "jpe" :: Nil)
  case object `svg+xml` extends ImageType("svg+xml" :: Nil)
  case object tiff extends ImageType("tiff" :: Nil)
  case object `vnd.microsoft.icon` extends ImageType("ico" :: Nil)

  /* Message Types */
  case object http extends MessageType("http" :: Nil)
  case object `delivery-status` extends MessageType("delivery-status" :: Nil)

  /* Multipart Types */
  case object mixed extends MultipartType("mixed" :: Nil)
  case object alternative extends MultipartType("alternative" :: Nil)
  case object related extends MultipartType("related" :: Nil)
  case object `form-data` extends MultipartType("form-data" :: Nil)
  case object signed extends MultipartType("signed" :: Nil)
  case object encrypted extends MultipartType("encrypted" :: Nil)

  /* Text Types */
  case object css extends TextType("css" :: Nil)
  case object csv extends TextType("csv" :: Nil)
  case object html extends TextType("html" :: "htm" :: Nil)
  case object plain extends TextType("c" :: "c++" :: "cc" :: "com" :: "conf" :: "f" :: "h" :: "jav" :: "pl" :: "text" :: "txt" :: Nil)
  case object xml extends TextType("xml" :: Nil)

  /* Video Types */
  case object quicktime extends VideoType("qt" :: "mov" :: Nil)
  case object `x-msvideo` extends VideoType("avi" :: Nil)

  /* Implicit Conversions */
  implicit def applicationTypeJavaScript2TextTypeJavaScript(appType: JavaScriptApplicationType): TextType = {
    case object TextTypeJavaScript extends TextType(appType.extensions) { 
      override def productPrefix = appType.subtype 
    }
    return TextTypeJavaScript
  }

  implicit def applicationTypeOgg2AudioTypeOgg(appType: OggApplicationType ): AudioType = {
    case object AudioTypeOgg extends AudioType(appType.extensions) {
      override def productPrefix = appType.subtype
    }
    return AudioTypeOgg
  }

  implicit def applicationTypeOgg2VideoTypeOgg(appType: OggApplicationType): VideoType = {
    case object VideoTypeOgg extends VideoType(appType.extensions) {
      override def productPrefix = appType.subtype 
    }
    return VideoTypeOgg
  }

  implicit def audioTypeMpeg2VideoTypeMpeg (audioType: MpegAudioType): VideoType = {
    case object VideoTypeMpeg extends VideoType(audioType.extensions) {
      override def productPrefix = audioType.subtype
    }
    return VideoTypeMpeg
  }

  implicit def audioTypeMp42VideoTypeMp4 (audioType: Mp4AudioType): VideoType = {
    case object VideoTypeMp4 extends VideoType(audioType.extensions) {
      override def productPrefix = audioType.subtype
    }
    return VideoTypeMp4
  }

  /* Constructor Methods */
  object application {
    def / (applicationType: ApplicationType) = new MimeType {
      def maintype = "application"
      def subtype = applicationType.subtype 
      override def extensions = applicationType.extensions
    }
  }

  object audio {
    def / (audioType: AudioType) = new MimeType {
      def maintype = "audio"
      def subtype = audioType.subtype 
      override def extensions = audioType.extensions
    }
  }

  object image {
    def / (imageType: ImageType) = new MimeType {
      def maintype = "image"
      def subtype = imageType.subtype 
      override def extensions = imageType.extensions
    }
  }

  object message {
    def / (messageType: MessageType) = new MimeType {
      def maintype = "message"
      def subtype = messageType.subtype 
      override def extensions = messageType.extensions
    }
  }

  object multipart {
    def / (multipartType: MultipartType) = new MimeType {
      def maintype = "multipart"
      def subtype = multipartType.subtype 
      override def extensions = multipartType.extensions
    }
  }

  object text {
    def / (textType: TextType) = new MimeType {
      def maintype = "text"
      def subtype = textType.subtype 
      override def extensions = textType.extensions
    }
  }
}

