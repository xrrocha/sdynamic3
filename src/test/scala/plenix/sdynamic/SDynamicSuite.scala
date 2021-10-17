package plenix.sdynamic

import scala.language.implicitConversions // rrc

class SDynamicSuite extends TestSuite:

  val data = Map(
    "name" -> "Alex",
    "age" -> "21",
    "birthdate" -> "11/24/1999",
    "languageSkills" -> List(
      Map("language" -> "English", "level" -> "advanced"),
      Map("language" -> "Spanish", "level" -> "advanced"),
      Map("language" -> "Japanese", "level" -> "intermediate"),
    ),
    "hobbies" -> Map(
      "animé" -> "a lot",
      "reading" -> "sure, on screen",
      "programming" -> "uh, yeah",
      "poetry" -> "as long as no free verse",
    ),
  )

  val person = SDynamic(data)

  test("Scala map is accessible through dynamic properties") {

    assert(person.name == "Alex")
    assert(person.birthdate == "11/24/1999")

    val languageSkills: Seq[SDynamic] = person.languageSkills
    assert(languageSkills.length == 3)
    assert(languageSkills(0).language == "English")
    assert(languageSkills(0).level == "advanced")

    val hobbies: Map[String, SDynamic] = person.hobbies
    assert(hobbies.size == 4)
    assert(hobbies.keySet == Set("animé", "reading", "programming", "poetry"))
    assert(hobbies("programming") == "uh, yeah")
  }

  test("Converts property to String") {
    val name: String = person.name
    assert(name == "Alex")
  }

  test("Converts property to Int") {
    val age: Int = person.age
    assert(age == 21)
  }

  test("Converts property to Double") {
    val age: Double = person.age
    assert(age == 21.0)
  }

  test("Converts property to List") {
    val languageSkills: List[SDynamic] = person.languageSkills
    assert(languageSkills.size == 3)
    assert(languageSkills == person.languageSkills.toList)
  }

  test("Converts property to Map") {
    val hobbies: Map[String, SDynamic] = person.hobbies
    assert(hobbies.size == 4)
    assert(hobbies.keySet == Set("animé", "reading", "programming", "poetry"))
    assert(hobbies == person.hobbies.toMap)
  }

  test("Fails for unmapped properties") {
    intercept[IllegalArgumentException] {
      SDynamic("nope").selectDynamic("nonExistent")
    }
  }

  test("Fails for non-indexed properties") {
    intercept[IllegalArgumentException] {
      SDynamic("nope").applyDynamic("nonExistent")(0)
    }
    intercept[IllegalArgumentException] {
      new SDynamic(Map("name" -> "Neo")).applyDynamic("name")(0)
    }
  }

  test("equals() uses value") {
    val value = "42"
    val dynamicValue = SDynamic(value)
    assert(dynamicValue == value)
    assert(dynamicValue.equals(value))
  }

  test("hasCode() uses value") {
    val value = "69"
    val dynamicValue = SDynamic(value)
    assert(dynamicValue.hashCode == value.hashCode)
  }

  test("toString() uses value") {
    val value = new java.util.Date
    val dynamicValue = SDynamic(value)
    assert(dynamicValue.toString == value.toString)
  }
