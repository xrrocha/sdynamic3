package plenix.sdynamic

import scala.util.parsing.combinator.*

object YamlParser extends RegexParsers :
  override def skipWhitespace = false

  val mapSeparator = ": *\r?\n?".r

  val seqIndicator = "- *\r?\n?".r

  val mappingKey = raw"[^-:\r\n\}\{\[]+".r

  val newLine = " *\r*\n+".r

  val inlineSeparator = " *, +".r

  val openBrace = raw"\{ *".r

  val closeBrace = raw" *\}".r

  val openBracket = raw"\[ *".r

  val closeBracket = raw" *]".r

  def parse(text: String): ParseResult[Any] = parse(yaml, text)

  def yaml: Parser[Any] =
    opt(newLine) ~> (list(0) | mappings(0))

  def list(numLeadingSpaces: Int): Parser[List[Any]] =
    inlineList | indentedList(numLeadingSpaces)

  def inlineList: Parser[List[Any]] =
    openBracket ~> repsep(nestedListData(0), inlineSeparator) <~ closeBracket

  def indentedList(numLeadingSpaces: Int): Parser[List[Any]] =
    rep1sep(
      leadingSpaces(numLeadingSpaces) ~ seqIndicator ~> nestedListData(numLeadingSpaces),
      newLine,
    )

  def nestedListData(numLeadingSpaces: Int): Parser[Any] =
    list(numLeadingSpaces + 1)
      | mappings(numLeadingSpaces)
      | scalarData(raw"[^,\r\n\]]+")

  def mappings(numLeadingSpaces: Int): Parser[Map[String, Any]] =
    (indentedMap(numLeadingSpaces) | inlineMap) ^^ { list =>
      list.toMap
    }

  def indentedMap(numLeadingSpaces: Int): Parser[List[(String, Any)]] =
    rep1sep(indentedMapping(numLeadingSpaces), newLine)

  def inlineMap: Parser[List[(String, Any)]] =
    openBrace ~> repsep(inlineMapping, inlineSeparator) <~ closeBrace

  def indentedMapping(numLeadingSpaces: Int): Parser[(String, Any)] =
    leadingSpaces(numLeadingSpaces) ~> mappingKey ~ mapSeparator ~
      (list(0) | mappings(numLeadingSpaces + 1) | scalarData(raw"[^\r\n]+")) ^^ {
      case key ~ _ ~ value => (key, value)
    }

  def inlineMapping: Parser[(String, Any)] =
    mappingKey ~ mapSeparator ~ (inlineList | inlineMap | scalarData(raw"[^,\r\n\}]+")) ^^ {
      case key ~ _ ~ value => (key, value)
    }

  def scalarData(regexString: String): Parser[String] =
    (raw"""'([^'\r\n]|\')*'|"([^"\r\n]|\")*"|""" + regexString).r ^^ { str =>
      unquote(str)
    }

  def leadingSpaces(numLeadingSpaces: Int): Parser[Int] =
    ("^ {" + numLeadingSpaces + ",}").r ^^ { str =>
      str.length
    }

  private def unquote(str: String) =
    if str.isEmpty then ""
    else
      val firstChar = str.head
      if firstChar == '\'' || firstChar == '"' then
        val unescaped = str.replaceAll("\\\\" + firstChar, firstChar.toString)
        unescaped.substring(1, unescaped.length - 1)
      else str.trim
