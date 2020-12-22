package jpeg.transcoder.entropy.coding.huffman

import com.davidjohnburrowes.format.jpeg.JpegData
import com.davidjohnburrowes.format.jpeg.component.DhtHuffmanTable
import com.davidjohnburrowes.format.jpeg.component.FrameComponent
import com.davidjohnburrowes.format.jpeg.component.SosComponentSpec
import com.davidjohnburrowes.format.jpeg.marker.DhtSegment
import com.davidjohnburrowes.format.jpeg.marker.SofSegment
import com.davidjohnburrowes.format.jpeg.marker.SosSegment
import commons.asBitsString
import kotlin.math.max

internal data class Component(
  val specsInfo: SosComponentSpec,
  val frameInfo: FrameComponent,
  val dcTable: ComponentHuffmanTable,
  val acTable: ComponentHuffmanTable
)

internal data class ComponentHuffmanTable(
  val dhtTable: DhtHuffmanTable,
  val codeToValue: Map<String, Byte>,
  val valueToCode: Map<Byte, String>
)

internal data class HuffmanJpegMeta(
  val sofSegment: SofSegment,
  val horizontalScalingMax: Int,
  val verticalScalingMax: Int,
  val mcuHorizontalSize: Int,
  val mcuVerticalSize: Int,
  val mcuWidth: Int,
  val mcuHeight: Int,
  val components: List<Component>
) {

  companion object {
    fun fromJpeg(jpeg: JpegData): HuffmanJpegMeta {
      var horizontalScalingMax = -1
      var verticalScalingMax = -1
      val sofSegment = jpeg.filterIsInstance<SofSegment>().first()
      val componentIdToFrame = (0 until sofSegment.componentCount).associate {
        val frame = sofSegment.getComponent(it)
        horizontalScalingMax = max(horizontalScalingMax, frame.horizontalScaling)
        verticalScalingMax = max(verticalScalingMax, frame.verticalScaling)
        frame.componentId to frame
      }
      val mcuHorizontalSize = horizontalScalingMax * 8
      val mcuVerticalSize = verticalScalingMax * 8
      val mcuWidth = (sofSegment.imageWidth + mcuHorizontalSize - 1) / mcuHorizontalSize
      val mcuHeight = (sofSegment.imageHeight + mcuVerticalSize - 1) / mcuVerticalSize

      val dhtTables = jpeg.filterIsInstance<DhtSegment>().flatMap { it.toList() }
      val componentHuffmanTables = dhtTables.map { dhtTable ->
        val codeToValue = mutableMapOf<String, Byte>()
        var currentCode = 0
        (1..16).forEach { codeLength ->
          val codeValues = dhtTable.getElement(codeLength - 1)
          codeValues.forEach { codeValue ->
            val codeValueBinary = currentCode.asBitsString(codeLength)
            codeToValue.put(codeValueBinary, codeValue.toByte())
            currentCode++
          }
          currentCode = currentCode shl 1
        }
        val valueToCode = codeToValue.map { (k, v) -> v to k }.toMap()
        require(codeToValue.size == valueToCode.size)
        ComponentHuffmanTable(dhtTable, codeToValue, valueToCode)
      }

      val sosSegment = jpeg.filterIsInstance<SosSegment>().first()
      val components = (0 until sosSegment.componentSpecCount).map { segmentIndex ->
        val specs = sosSegment.getComponentSpec(segmentIndex)
        val componentId = specs.componentSelector
        val frameInfo = componentIdToFrame.getValue(componentId)
        val dcTable = componentHuffmanTables.first {
          it.dhtTable.tableClass == 0.toByte() && it.dhtTable.tableId == specs.dcTableSelector
        }
        val acTable = componentHuffmanTables.first {
          it.dhtTable.tableClass == 1.toByte() && it.dhtTable.tableId == specs.acTableSelector
        }
        Component(specs, frameInfo, dcTable, acTable)
      }

      return HuffmanJpegMeta(
          sofSegment = sofSegment,
          horizontalScalingMax = horizontalScalingMax,
          verticalScalingMax = verticalScalingMax,
          mcuHorizontalSize = mcuHorizontalSize,
          mcuVerticalSize = mcuVerticalSize,
          mcuWidth = mcuWidth,
          mcuHeight = mcuHeight,
          components = components
      )
    }
  }
}