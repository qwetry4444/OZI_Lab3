import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


const val MESSAGE_BITS_PER_CHANNEL = 3
const val END_MARKER = '\u0003'


fun main() {
    val srcFilePath = "C:\\Users\\Maks\\IdeaProjects\\OZI_Lab3\\src\\hobbies.png"
    val dstFilePath = "C:\\Users\\Maks\\IdeaProjects\\OZI_Lab3\\src\\LogoAfter.png"
    val srcImage = ImageIO.read(File(srcFilePath))
    val message = "123"

    
    hideMessage(srcImage, File(dstFilePath), message)
    val dstImage = ImageIO.read(File(dstFilePath))

    val retrievedMessage = getMessageFromImage(dstImage)
    println("Полученное сообщение: $retrievedMessage")
}

fun hideMessage(srcImage: BufferedImage, dstImageFile: File, msg: String) {
    val messageBitsIterator = (msg + END_MARKER).toByteArray().flatMap { byte ->
        (7 downTo 0).map { (byte.toInt() shr it) and 1 }
    }.toMutableList().iterator()

    for (x in 0 until srcImage.width) {
        for (y in 0 until srcImage.height) {
            if (putMessagePartToPixelOrFinish(messageBitsIterator, srcImage, x, y)) {
                ImageIO.write(srcImage, "png", dstImageFile)
                return
            }
        }
    }
}

fun putMessagePartToPixelOrFinish(messageIterator: Iterator<Int>, image: BufferedImage, x: Int, y: Int) : Boolean {
    val pixel = image.getRGB(x, y)
    val channels = getPixelChannels(pixel)

    for (i in channels.indices) {
        var bitsValue = 0

        repeat(MESSAGE_BITS_PER_CHANNEL) {
            bitsValue = bitsValue shl 1
            if (messageIterator.hasNext()) {
                bitsValue = bitsValue or messageIterator.next()
            } else {
                channels[i] = (channels[i] and ((1 shl MESSAGE_BITS_PER_CHANNEL) - 1).inv()) or bitsValue
                val newColor = (channels[0] shl 24) or (channels[1] shl 16) or (channels[2] shl 8) or channels[3]
                image.setRGB(x, y, newColor)
                return true
            }
        }

        val mask = ((1 shl MESSAGE_BITS_PER_CHANNEL) - 1).inv()
        channels[i] = (channels[i] and mask) or bitsValue
    }

    val newColor = (channels[0] shl 24) or (channels[1] shl 16) or (channels[2] shl 8) or channels[3]
    image.setRGB(x, y, newColor)
    return false
}

fun getPixelChannels(pixel: Int) : IntArray {
    val a = pixel shr 24 and 0xFF
    val r = pixel shr 16 and 0xFF
    val g = pixel shr 8 and 0xFF
    val b = pixel and 0xFF
    return intArrayOf(a, r, g, b)
}

fun getMessageFromImage(imageWithMessage: BufferedImage): String {
    val bitsList = mutableListOf<Int>()

    for (x in 0 until imageWithMessage.width) {
        for (y in 0 until imageWithMessage.height) {
            val channels = getPixelChannels(imageWithMessage.getRGB(x, y))

            for (i in channels.indices) {
                val mask = (1 shl MESSAGE_BITS_PER_CHANNEL) - 1
                val hiddenBits = channels[i] and mask

                for (bitPos in MESSAGE_BITS_PER_CHANNEL - 1 downTo 0) {
                    bitsList.add((hiddenBits shr bitPos) and 1)
                }
            }
        }
    }

    val bytes = bitsList.chunked(8).map { bits ->
        bits.fold(0) { acc, bit -> (acc shl 1) or bit }
    }

    val message = bytes
        .takeWhile { it != END_MARKER.code }
        .map { it.toByte().toInt().toChar() }
        .joinToString("")

    return message
}