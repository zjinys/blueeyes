package blueeyes.core.service

import org.specs.Specification
import scala.util.matching.Regex
import RestPathPatternImplicits._

import blueeyes.core.http.{HttpRequest, HttpMethods}

class RestPathPatternSpec extends Specification{
  import RestPathPatternImplicits._

  "path matching and regex extraction" should {
    "match regexp element with first named group" in{
      testPath("(?<bar>[a-z]+)", List(("foo", Map('bar -> "foo"))), List("1970"))
    }
    "match regexp element with null named group" in{
      testPath("([1-2]+)((?<foo>[1-2]+)?)", List(("1", Map('foo -> ""))), List())
    }
    "match regexp element with not first named group" in{
      testPath("(foo(?<bar>baz))", List(("foobaz", Map('bar -> "baz"))), List("barfoo"))
    }
    "match regexp element without named group" in{
      testPath("(foo)", List(("foo", Map())), List("1970"))
      testPath("(?:foo)", List(("foo", Map())), List("1970"))
    }
    "match regexp element multiple named group" in{
      testPath("((?<bar>[a-z]+)-(?<foo>[1-9]+))", List(("foo-123", Map('bar -> "foo", 'foo -> "123"))), List("1970"))
    }
    "match regexp element nested in NamedCaptureGroup" in{
      testPath("(foo(?<bar>baz(?<foo>[1-9]+)))", List(("foobaz123", Map('bar -> "baz123", 'foo -> "123"))), List("1970"))
    }
  }

  "path matching and symbol extraction" should {
    "match correct literal path containing a single path element" in {
      testPath("/foo",
        List(("/foo", Map())),
        List("/bar")
      )
    }
    "match correct literal path containing many path elements" in {
      testPath("/foo/bar/baz",
        List(("/foo/bar/baz", Map())),
        List("/bar/baz/bar")
      )
    }
    "match correct literal and symbol path containing many path elements" in {
      testPath("/foo/'bar/baz",
        List(("/foo/baz/baz", Map('bar -> "baz")), ("/foo/boo/baz", Map('bar -> "boo")), ("/foo/foo/baz", Map('bar -> "foo"))),
        List("/bar/bar/bar")
      )
    }
    "match root path" in {
      testPath("/",
        List(("/", Map())),
        List("")
      )
    }
    "create parameters that have characters which are not valid for symbols themselves" in {
      testPath("/'foo",
        List(("/foo_bar-baz", Map('foo -> "foo_bar-baz"))),
        List("")
      )
    }
    "not match more than specified when end method is invoked" in {
      RestPathPattern("/get/'foo").$.isDefinedAt("/foo/bar") mustEqual(false)
    }
    "create parameters for regression case" in {
      testPath("/get/'foo",
        List(("/get/foo-value", Map('foo -> "foo-value")), ("/get/name%20name2", Map('foo -> "name%20name2")), ("/get/name name2", Map('foo -> "name name2"))),
        List("/foo/bar")
      )
    }
  }

  "slash operator" should {
    "combine symbols and literals using slash operator" in {
      val pattern: RestPathPattern = "/foo" / 'bar / 'biz / "blah"

      pattern("/foo/a/b/blah") mustEqual(Map('bar -> "a", 'biz -> "b"))
    }
    "work starting from root" in {
      (RestPathPattern.Root / "foo").isDefinedAt("/foo") mustEqual(true)
    }
    "match complex path with symbols" in {
      (RestPathPattern.Root / "foo" / "bar" / 'param).isDefinedAt("/foo/bar/value") mustEqual(true)
    }
    "create single parameter" in {
      (RestPathPattern.Root / 'param).apply("/value") mustEqual(Map[Symbol, String]('param -> "value"))
    }
    "create multiple parameters" in {
      (RestPathPattern.Root / 'param1 / 'param2).apply("/value1/value2") mustEqual(Map[Symbol, String]('param1 -> "value1", 'param2 -> "value2"))
    }
    "create single parameter in lengthy literal path" in {
      (RestPathPattern.Root / "foo" / "bar" / 'param).apply("/foo/bar/value") mustEqual(Map[Symbol, String]('param -> "value"))
    }
    "should optionally accept slash when option variant is used" in {
      (("foo": RestPathPattern) /?).apply("foo/") mustEqual(Map())
      (("foo": RestPathPattern) /?).apply("foo") mustEqual(Map())
    }
  }

  "end symbol" should {
    "match end of path when final element is symbol" in {
      ("/foo/bar/'param" $).apply("/foo/bar/value") mustEqual(Map('param -> "value"))
    }
    "not match beyond end of path when final element is symbol" in {
      ("/foo/bar/'param" $).isDefinedAt("/foo/bar/value/") mustBe(false)
    }
    "match end of path when final element is string" in {
      ("/foo/bar/adCode.html" $).apply("/foo/bar/adCode.html") mustEqual(Map())
    }
    "not match beyond end of path when final element is string" in {
      ("/foo/bar/adCode.html" $).isDefinedAt("/foo/bar/adCode.html2") mustBe(false)
    }
  }

  "`...` operator" should {
    "match trailing string" in {
      ("/foo" `...` ('rest)).apply("/foo/bar") mustEqual(Map('rest -> "/bar"))
    }
  }

  /* ---- Regex Tests ---- */
  "Regular expression pattern" should {
    "match for a simple pattern" in {
      (RestPathPattern.Root/ "foo" / "bar" / new Regex("""(steamboats)""", "id") ~ List('id)).isDefinedAt("/foo/bar/steamboats") mustEqual(true)
    }
    "not match for a simple pattern"  in {
      (RestPathPattern.Root/ "foo" / "bar" / new Regex("""(steamboats)""", "id") ~ List('id)).isDefinedAt("/foo/bar/lame_boats") mustEqual(false)
    }
    "not match for when the match occurs but later in the string" in {
      (RestPathPattern.Root/ "foo" / "bar" / new Regex("""(steamboats)""", "id") ~ List('id)).isDefinedAt("/foo/bar/lame_steamboats") mustEqual(false)
    }
    "match a more complex pattern" in {
      (RestPathPattern.Root/ "foo" / "bar" / new Regex("""([a-z]+_[0-9])""", "id") ~ List('id)).isDefinedAt("/foo/bar/hercules_1") mustEqual(true)
    }
    "not match for a more complex pattern" in {
      (RestPathPattern.Root/ "foo" / "bar" / new Regex("""([a-z]+_[0-9])""", "id") ~ List('id)).isDefinedAt("/foo/bar/HadesSux") mustEqual(false)
    }
   "match a complex pattern with regexp with named capturing group" in {
      (RestPathPattern.Root/ "foo" / "bar" / """(?<id>[a-z]+_[0-9])""").isDefinedAt("/foo/bar/HadesSux") mustEqual(false)
    }
    "match for the other syntax and positive look ahead" in {
      ("/foo/bar" / new Regex("""([a-z]+)(\.html)""", "path") ~ List('path) $).isDefinedAt("/foo/bar/example.html") mustBe (true)
    }
    "use the implicit for Regex (removed the $)" in {
      ("/foo/bar" / new Regex("""([a-z]+)(\.html)""", "path") ~ List('path)).isDefinedAt("/foo/bar/example.html") mustBe (true)
    }
    "recover the parameter with positive look ahead" in {
      val pattern: RestPathPattern = "/darth" / new Regex("""([a-z]+)(\.gif)""", "path") ~ List('path)
      pattern.apply("/darth/joshuar.gif").mustEqual(Map[Symbol, String]('path -> "joshuar"))
    }
  }

  "Symbol pattern" should {
    "match string with period" in {
      ("/foo/bar/'name" $).isDefinedAt("/foo/bar/foocubus.gif") mustBe (true)
    }
  }

  "implicits" should {
    "create parameters automatically for complex path specified as string" in {
      val pattern: RestPathPattern = "/foo/bar/'param"

      pattern.apply("/foo/bar/value") mustEqual(Map[Symbol, String]('param -> "value"))
    }
  }

  "shift" should {
    "shift subpath leftward by matched pattern" in {
      val pattern: RestPathPattern = "/foo/'param"

      pattern.shift(HttpRequest(method = HttpMethods.GET, uri = "/foo/bar/baz")) mustEqual(HttpRequest(method = HttpMethods.GET, uri = "/foo/bar/baz").withSubpath("/baz"))
    }
  }

  private def testPath(path: String, isDefinedAt: List[(String, Map[Symbol, String])], isNotDefinedAt: List[String]) {
    val pattern = RestPathPattern(path)

    isDefinedAt.foreach { pair =>
      val path = pair._1
      val map  = pair._2

      pattern.isDefinedAt(path) mustEqual(true)
      pattern.apply(path) mustEqual(map)
    }

    isNotDefinedAt.foreach { path =>
      pattern.isDefinedAt(path) mustEqual(false)
    }
  }
}
