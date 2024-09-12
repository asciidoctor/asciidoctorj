package org.asciidoctor.converter

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import spock.lang.Specification

class TableConverterTest extends Specification {

    private Asciidoctor asciidoctor = Asciidoctor.Factory.create()

    def "should convert a table with horizontal alignment"() {
        given:
        String document = '''
= Hello Table

[chapter]
== A chapter

.Simple table with single text rows and a header
[options="header,footer"]
[cols="<,^,^"]
|====
|       | Column A | Column B

| Row 1 |   A1     |    B1
| Row 2 |   A2     |    B2

| Sum   | Sum A    | Sum B
|====
'''
        asciidoctor.javaConverterRegistry().register(TableTestConverter, 'tabletestconverter')

        when:
        def options = Options.builder().standalone(false).backend('tabletestconverter').build()
        String content = asciidoctor.convert(document, options)

        then:
        content.readLines().collect { it - ~/\s+$/ } == '''HELLO TABLE

-- A chapter --

               |   Column A    |   Column B
---------------+---------------+---------------
Row 1          |      A1       |      B1
Row 2          |      A2       |      B2
===============#===============#===============
Sum            |     Sum A     |     Sum B'''.readLines()
    }


    def "should convert a table with nested asciidoctor content"() {

        given:
        String document = '''
= Hello Asciidoctor Table

[cols="2"]
|====
a|
= A header
| Second column
|====
'''
        asciidoctor.javaConverterRegistry().register(TableTestConverter, 'tabletestconverter')

        when:
        def options = Options.builder().standalone(false).backend('tabletestconverter').build()
        String content = asciidoctor.convert(document, options)

        then:
        content.readLines().collect { it - ~/\s+$/ } == '''HELLO ASCIIDOCTOR TABLE

A HEADER       |Second column'''.readLines()

    }

    def "should register a converter instance"() {

        given:
        String document = '''
= Hello Asciidoctor Table

[cols="2"]
|====
a|
= A header
| Second column
|====
'''
        asciidoctor.javaConverterRegistry().register(new TableTestConverter(), 'tabletestconverter')

        when:
        String content = asciidoctor.convert(document, OptionsBuilder.options().headerFooter(false).backend('tabletestconverter'))

        then:
        content.readLines().collect {it - ~/\s+$/ } == '''HELLO ASCIIDOCTOR TABLE

A HEADER       |Second column'''.readLines()

    }
}
