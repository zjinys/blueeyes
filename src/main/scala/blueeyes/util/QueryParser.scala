package blueeyes.util

object QueryParser {
  def parseQuery(query: String): Map[Symbol, String] = Map((query.split("&").toList.flatMap { nameEqualsValue =>
    nameEqualsValue.split("=").toList match {
      case "" :: Nil => Nil
      case name :: value :: Nil => (name, value) :: Nil
      case name :: Nil => (name, "") :: Nil
      case name :: everythingElse => (name, everythingElse.mkString("=")) :: Nil
      case Nil => Nil
    }
  }).map { nameValue =>
    import java.net.URLDecoder._

    (Symbol(decode(nameValue._1, "UTF-8")), decode(nameValue._2, "UTF-8"))
  }: _*)

  def unparseQuery(query: Map[Symbol, String]): String = query.map { nameValue =>
    import java.net.URLEncoder._

    encode(nameValue._1.name, "UTF-8") + "=" + encode(nameValue._2, "UTF-8")
  }.mkString("&")
}