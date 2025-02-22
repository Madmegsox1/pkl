package org.pkl.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pkl.core.util.IoUtils
import java.io.StringWriter

class IniRendererTest {
  @Test
  fun `render document`() {
    val evaluator = Evaluator.preconfigured()
    val module = evaluator.evaluate(ModuleSource.modulePath("org/pkl/core/iniRendererTest.pkl"))
    val writer = StringWriter()
    val renderer = ValueRenderers.ini(writer, true, false)

    renderer.renderDocument(module)
    val output = writer.toString()
    val expected = IoUtils.readClassPathResourceAsString(javaClass, "iniRendererTest.ini")
    

    assertThat(output).isEqualTo(expected)
  }

  @Test
  fun `render unsupported document values`() {
    val unsupportedValues = listOf(
      "List()", "new Listing {}", "Map()", "new Mapping {}", "Set()",
      "new PropertiesRenderer {}", "new Dynamic {}"
    )

    unsupportedValues.forEach {
      val evaluator = Evaluator.preconfigured()
      val renderer = ValueRenderers.ini(StringWriter(), true, false)

      val module = evaluator.evaluate(ModuleSource.text("value = $it"))
      assertThrows<RendererException> { renderer.renderValue(module) }
    }
  }

  @Test
  fun `rendered document ends in newline`() {
    val module = Evaluator.preconfigured()
      .evaluate(ModuleSource.text("foo { bar = 0 }"))

    for (omitNullProperties in listOf(false, true)) {
      for (restrictCharSet in listOf(false, true)) {
        val writer = StringWriter()
        ValueRenderers.ini(writer, omitNullProperties, restrictCharSet).renderDocument(module)
        assertThat(writer.toString()).endsWith("\n")
      }
    }
  }
}
