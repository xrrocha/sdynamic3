package plenix.sdynamic

object Example:
  def main(args: Array[String]): Unit =

    val countries = sdyaml {"""
         |- name: USA
         |  currency: USD
         |  population: 331.5
         |  motto: In God We Trust
         |  languages: [ English ]
         |  flag: usa.webp
         |- name: Canada
         |  currency: CAD
         |  population: 38.0
         |  motto: A Mari Usque ad Mare<br> (<i>From sea to sea, D'un océan à l'autre</i>)
         |  languages: [ English, French ]
         |  flag: canada.webp
         |- name: Mexico
         |  currency: MXN
         |  population: 130.5
         |  motto: Patria, Libertad, Trabajo y Cultura<br> (<i>Homeland, Freedom, Work and Culture</i>)
         |  languages: [ Spanish ]
         |  flag: mexico.webp
         |""".stripMargin
    }
      .toList

    def country2Html(country: SDynamic) = s"""
         |<tr>
         |  <td><img src="${country.flag}"></td>
         |  <td>${country.name}</td>
         |  <td>${country.motto}</td>
         |  <td>
         |    <ul>
         |      ${country.languages.toList.map(language => s"<li>$language</li>").mkString}
         |    </ul>
         |  </td>
         |</tr>
         |""".stripMargin

    val pageHtml = s"""
         |<html>
         |  <head>
         |    <meta charset="UTF-8">
         |    <title>NAFTA Countries</title>
         |    <style>
         |      table { margin-left: auto; margin-right: auto; border-style: solid; }
         |      th { border: 2px solid;}
         |      td { border: 2px solid; text-align: center; vertical-align: middle; }
         |      ul { text-align: left; }
         |  </style>
         |  </head>
         |    <body>
         |      <table>
         |      <tr>
         |        <th>Flag</th>
         |        <th>Name</th>
         |        <th>Motto</th>
         |        <th>Languages</th>
         |      </tr>
         |      <tr>${countries.map(country2Html).mkString}</tr>
         |    </table>
         |  </body>
         |</html>
         |""".stripMargin

    val out = new java.io.FileWriter("src/test/resources/countries.html")
    try
      out.write(pageHtml)
    finally
      out.flush()
      out.close()
