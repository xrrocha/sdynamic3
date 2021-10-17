package plenix.sdynamic

import scala.language.dynamics

class SDynamic(_value: Any) extends Dynamic:

  val value: String | List[SDynamic] | Map[String, SDynamic] =
    _value.asInstanceOf[Matchable] match
      case list: List[?] => list.map(SDynamic(_))
      case map: Map[?, ?] => map.map((k, v) => (k.toString, SDynamic(v)))
      case _ => _value.toString

  def selectDynamic(name: String): SDynamic =
    try value.asInstanceOf[Map[String, SDynamic]](name)
    catch case e: Exception => throw IllegalArgumentException(s"No such property: '$name'. ($value)")

  def applyDynamic(name: String)(index: Int): SDynamic =
    try value.asInstanceOf[Map[String, SDynamic]](name).asInstanceOf[List[SDynamic]](index)
    catch case e: Exception => throw IllegalArgumentException(s"Property '$name' can't be indexed")

  def toInt = value.toString.toInt

  def toDouble = value.toString.toDouble

  def toList: List[SDynamic] = value.asInstanceOf[List[SDynamic]]

  def toMap: Map[String, SDynamic] = value.asInstanceOf[Map[String, SDynamic]]

  override def equals(other: Any) =
    if other.isInstanceOf[SDynamic] then value.equals(other.asInstanceOf[SDynamic].value)
    else value.equals(other)

  override def hashCode = value.hashCode

  override def toString = value.toString

object SDynamic:
  given CanEqual[SDynamic, String] = CanEqual.derived
  given Conversion[SDynamic, String] = _.value.toString

  given CanEqual[SDynamic, Int] = CanEqual.derived
  given Conversion[SDynamic, Int] = _.value.toString.toInt

  given CanEqual[SDynamic, Double] = CanEqual.derived
  given Conversion[SDynamic, Double] = _.value.toString.toDouble

  given CanEqual[SDynamic, List[SDynamic]] = CanEqual.derived
  given Conversion[SDynamic, List[SDynamic]] = _.toList

  given CanEqual[SDynamic, Map[String, SDynamic]] = CanEqual.derived
  given Conversion[SDynamic, Map[String, SDynamic]] = _.toMap
