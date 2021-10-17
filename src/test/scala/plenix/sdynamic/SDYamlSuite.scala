package plenix.sdynamic

class SDYamlSuite extends TestSuite:
  test("sdyaml builds correct payload") {
    assert(sdyaml("- { name: Neo, age: 42 }") == SDynamic(List(Map("name" -> "Neo", "age" -> "42"))))
  }
