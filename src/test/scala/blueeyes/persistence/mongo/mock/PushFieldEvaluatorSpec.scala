package blueeyes.persistence.mongo.mock

import org.specs.Specification
import blueeyes.json.JsonAST._
import com.mongodb.MongoException
import MockMongoUpdateEvaluators._
import blueeyes.persistence.mongo._

class PushFieldEvaluatorSpec  extends Specification{
  "create new Array for not existing field" in {
    val operation = ("foo" inc (MongoPrimitiveInt(2))).asInstanceOf[MongoUpdateField]
    PushFieldEvaluator(JNothing, operation.filter) mustEqual(JArray(JInt(2) :: Nil))
  }
  "add new element existing field" in {
    val operation = ("foo" inc (MongoPrimitiveInt(3))).asInstanceOf[MongoUpdateField]
    PushFieldEvaluator(JArray(JInt(2) :: Nil), operation.filter) mustEqual(JArray(JInt(2) :: JInt(3) :: Nil))
  }
  "cannot push to not Array field" in {
    val operation = ("foo" inc (MongoPrimitiveInt(3))).asInstanceOf[MongoUpdateField]
    PushFieldEvaluator(JInt(2), operation.filter) must throwA[MongoException]
  }
}