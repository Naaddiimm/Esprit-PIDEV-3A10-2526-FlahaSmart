package utilies;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class QRCodeGenerator {
    public static String generateQRCode(String data, int id) throws WriterException, IOException {
        Path dirPath = FileSystems.getDefault().getPath("qrcodes");
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String filePath = "qrcodes/produit_" + id + ".png";
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 200, 200);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        return filePath;
    }
}