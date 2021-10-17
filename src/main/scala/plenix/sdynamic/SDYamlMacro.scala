package plenix.sdynamic

inline def sdyaml(inline yamlString: String): SDynamic = ${ sdyamlImp('yamlString) }

import scala.quoted.*
def sdyamlImp(yamlString: Expr[String])(using Quotes): Expr[SDynamic] =
  import quotes.reflect.report

  val stringLiteral =
    yamlString match
      case '{ augmentString($str).stripMargin } => str
      case '{ augmentString($str) } => str
      case '{ $str: String } => str
      case _ => report.throwError(s"Expected constant Yaml string, got: ${yamlString.show}")
  val stringLiteralValue = stringLiteral.valueOrError.stripMargin

  val yamlResult =
    try YamlParser.parse(stringLiteralValue).get
    catch case e: Exception => report.error(s"Malformed yaml string: ${e.getMessage}")

  given ToExpr[Any] with
    def apply(any: Any)(using Quotes): Expr[String | List[Any] | Map[String, Any]] =
      any.asInstanceOf[Matchable] match
        case map: Map[?, ?] => Expr(map.asInstanceOf[Map[String, Any]])
        case list: List[?] => Expr(list.asInstanceOf[List[Any]])
        case _ => Expr(any.toString)

  '{ SDynamic(${Expr(yamlResult)}) }
