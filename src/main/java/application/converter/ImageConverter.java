package application.converter;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public abstract class ImageConverter extends ItemsConverter {
    protected String getImage(Map<String, MultipartFile> files, Integer iterator, String field) {
        return convertToBase64(files, iterator, field, 1920, 1080);
    }

    protected String getThumbnail(Map<String, MultipartFile> files, Integer iterator, String field) {
        return convertToBase64(files, iterator, field, 480, 360);
    }

    private String convertToBase64(Map<String, MultipartFile> files, Integer iterator, String field, double desiredWidth, double desiredHeight) {
        return getValue(files, iterator, field)
                .map(multipartFile -> {
                    try {
                        return multipartFile.getBytes();
                    } catch (IOException e) {
                        throw new IllegalStateException("Image processing failed.", e);
                    }
                })
                .filter(bytes -> bytes.length > 0)
                .map(bytes -> reduceQuality(bytes, desiredWidth, desiredHeight))
                .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                .map("data:image/jpeg;base64,"::concat)
                .orElse(null);
    }

    private byte[] reduceQuality(byte[] bytes, double desiredWidth, double desiredHeight) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            int width = image.getWidth();
            int height = image.getHeight();
            double scale = Math.min(desiredWidth / width, desiredHeight / height);
            width = (int) (width * scale);
            height = (int) (height * scale);
            Thumbnails.of(image)
                    .size(width, height)
                    .outputQuality(Float.NaN)
                    .outputFormat("jpeg")
                    .toOutputStream(outputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Image compression failed.", e);
        }
        return outputStream.toByteArray();
    }
}
