package plenix.sdynamic

class YamlParserSuite extends TestSuite:

  test("Supports unquoted scalar values") {
    val country = raw"{ name: USA, population: 331.5, motto:    In God We Trust    }"
    assert(parse(country) == Map("name" -> "USA", "population" -> "331.5", "motto" -> "In God We Trust"))
  }

  test("Supports single-quoted scalar values") {
    val country = raw"{ name: Mexico, population: 130.5, motto: 'Patria, Libertad, Trabajo y Cultura' }"
    assert(parse(country) == Map(
      "name" -> "Mexico", "population" -> "130.5", "motto" -> "Patria, Libertad, Trabajo y Cultura"))
  }

  test("Supports double-quoted scalar values") {
    val country = """{ name: Mexico, population: 130.5, motto: "Patria, Libertad, Trabajo y Cultura" }"""
    assert(parse(country) == Map(
      "name" -> "Mexico", "population" -> "130.5", "motto" -> "Patria, Libertad, Trabajo y Cultura"))
  }

  test("Honors escaped single quotes") {
    val cases = List(
      raw"{ value: '\'heading\' works' }" -> "'heading' works",
      raw"{ value: 'works with \'trailing\'' }" -> "works with 'trailing'",
      raw"{ value: 'One embedded (\'value\') works' }" -> "One embedded ('value') works",
      raw"{ value: 'Two embedded (\'first\' and \'second\') work' }" -> "Two embedded ('first' and 'second') work",
      raw"{ value: '\'heading\', \'middle\' and \'trailing\'' }" -> "'heading', 'middle' and 'trailing'",
    )
    cases.foreach((yaml, expected) => assert(expected == parse(yaml).asInstanceOf[Map[String, Any]]("value")))
  }

  test("Honors escaped double quotes") {
    val singleQuotedEscaped = """{ value: "Escaped \"value\" and \"value\" works!" }"""
    assert(parse(singleQuotedEscaped) == Map("value" -> """Escaped "value" and "value" works!"""))
  }

  test("Preserves surrounding spaces in single-quoted scala values") {
    val singleQuoted = "{ value: '  Spaced   ' }"
    assert(parse(singleQuoted) == Map("value" -> "  Spaced   "))
  }

  test("Preserves surrounding spaces in double-quoted scala values") {
    val doubleQuoted = """{ value: "  Spaced   " }"""
    assert(parse(doubleQuoted) == Map("value" -> "  Spaced   "))
  }

  test("General test") {
    val naftaCountries = sdyaml {"""
      - { name: USA,    population: 331.5, languages: [ English ] }
      - { name: Mexico, population: 130.5, languages: [ English ] }
      - { name: Canada, population: 38.0,  languages: [ English, French ] }
    """}
        .toList

    assert(naftaCountries.length == 3)
    assert(naftaCountries(0).name == "USA")
    assert(naftaCountries(1).population.toDouble == 130.5)
    assert(naftaCountries(2).languages.toList == Seq("English", "French"))
  }

  test("Builds empty inline map") {
    assert(Map() == parse("{}"))
  }

  test("Builds empty inline list") {
    assert(List() == parse("[]"))
  }

  test("Builds list with empty inline map") {
    assert(
      List("foo", Map(), "bar") ==
        parse("""
           - foo
           - {}
           - bar
       """)
    )
  }

  test("Builds map with empty inline list") {
    assert(
      Map("foo bar" -> "true", "snafu" -> List(), "empty" -> Map()) ==
        parse("""
           foo bar: true
           snafu: []
           empty: {}
       """)
    )
  }

  test("Builds simple map") {
    assert(Map("key" -> "value") == parse("""key: value"""))
    assert(
      Map("key1" -> "value1", "key2" -> "value2") ==
        parse("""
           key1: value1
           key2: value2
        """)
    )
    assert(
      Map("key 1" -> "value 1", "key 2" -> "value 2") ==
        parse("""
           key 1: value 1
           key 2: value 2
       """)
    )
  }

  test("Builds simple list") {
    assert(List("item1") == parse("""
         - item1
     """))
    assert(List("item1", "item2") == parse("""
        - item1
        - item2
    """))
  }

  test("Builds nested list") {

    // Bug: Yields List(List(item11, item12, List(item21, item22))), df?
    // assert(
    //   List(List("item11", "item12"), List("item21", "item22")) ==
    //     parse("""
    //       -
    //          - item11
    //          - item12
    //       -
    //          - item21
    //          - item22
    //    """)
    // )

    // Bug: Requires space after comma, bummer!
    assert(List(List("item11","item12"), List("item21", "item22")) ==
      parse("[[item11, item12], [item21, item22]]"))
  }

  test("Builds list of maps") {
    assert(
      List(
        Map("name" -> "John Smith", "age" -> "33"),
        Map("name" -> "Mary Smith", "age" -> "27"),
      ) ==
        parse("""
           - name: John Smith
             age: 33
           - name: Mary Smith
             age: 27
       """)
    )
  }

  test("Builds list of maps from objects") {
    assert(List(
      Map("name" -> "USA", "population" -> "331.5", "languages" -> List("English")),
      Map("name" -> "Mexico", "population" -> "130.5", "languages" -> List("English")),
      Map("name" -> "Canada", "population" -> "38.0", "languages" -> List("English", "French"))) ==
      parse(
        """
          |- { name: USA,  population: 331.5, languages: [ English ] }
          |- { name: Mexico,  population: 130.5, languages: [ English ] }
          |- { name: Canada,  population: 38.0, languages: [ English, French ] }
          |""".stripMargin)    )
  }

  test("Builds map of lists") {
    assert(
      Map(
        "men" -> List("John Smith", "Bill Jones"),
        "women" -> List("Mary Smith", "Susan Williams"),
      ) ==
        parse("""
             men:
               - John Smith
               - Bill Jones
             women:
               - Mary Smith
               - Susan Williams
       """)
    )
  }

  test("Builds inline map") {
    assert(Map("key" -> "value") == parse("""key: value"""))
    assert(
      Map("key1" -> "value1", "key2" -> "value2") == parse("""{ key1: value1, key2: value2 }""")
    )
  }

  test("Builds nested map") {
    assert(
      Map("JFrame" -> Map("name" -> "myFrame", "title" -> "My App Frame")) ==
        parse("""
           JFrame:
                name: myFrame
                title: My App Frame
       """)
    )
  }
  test("Builds nested map with empty element") {
    assert(
      Map("JFrame" -> Map("content" -> Map("button" -> "press"))) ==
        parse("""
           JFrame:
                content:
                button: press
       """)
    )
  }

  test("Builds simple bracketed list") {
    assert(
      List("item1", "item2") ==
        parse("""[ item1, item2 ]""")
    )
  }

  test("Builds map with bracketed lists") {
    assert(
      Map(
        "men" -> List("John Smith", "Bill Jones"),
        "women" -> List("Mary Smith", "Susan Williams"),
      ) ==
        parse("""
             men: [ John Smith, Bill Jones ]
             women: [ Mary Smith, Susan Williams ]
        """)
    )
  }

  test("Builds more complex graph") {
    assert(
      Map(
        "address" ->
          Map(
            "first_name" -> "Brian",
            "last_name" -> "Reece",
            "email" -> "brian@majordomo.com",
            "company" ->
              Map(
                "name" -> "Five Apart, Ltd.",
                "street_address" -> "8458 5th Street, San Francisco, CA 94107",
              ),
          )
      ) ==
        parse("""
          address:
             first_name: Brian
             last_name: Reece
             email: brian@majordomo.com
             company:
                name: Five Apart, Ltd.
                street_address: 8458 5th Street, San Francisco, CA 94107
        """)
    )
  }

  def parse(s: String): Any = YamlParser.parse(s).get
