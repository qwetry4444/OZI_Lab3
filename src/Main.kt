import java.io.File
import javax.imageio.ImageIO


fun main() {
    val srcFilePath = "C:\\Users\\Maksim\\Desktop\\uni\\sem 7\\ОЗИ\\3Лб\\OZI_Lab3_Steganography\\src\\hobbies.png"
    val destFilePath = "C:\\Users\\Maksim\\Desktop\\uni\\sem 7\\ОЗИ\\3Лб\\OZI_Lab3_Steganography\\src\\LogoAfter.png"
    val message = "dljkg5h8ypw58gjpP#$*(THRP(EHV    "

    hideMessage(srcFilePath, destFilePath, message)
}

fun hideMessage(srcFilePath: String, dstFilePath: String, msg: String) {
    val image = ImageIO.read(File(srcFilePath))
    var messageByteArray = msg.toByteArray()
    var messageBitNumber = 0
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            image.setRGB(x, y, image.getRGB(x, y) and 0x000FFFFF.toInt())
            messageBitNumber = (messageBitNumber + 1) % msg.length
        }
    }

    ImageIO.write(image, "png", File(dstFilePath))

}
