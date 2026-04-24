package net.outsideworx.services.converter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageConverterTest {
    private static final class TestConverter extends ImageConverter {
        private String testGetImage(Map<String, MultipartFile> files, int iterator, String field) {
            return getImage(files, iterator, field);
        }

        private String testGetThumbnail(Map<String, MultipartFile> files, int iterator, String field) {
            return getThumbnail(files, iterator, field);
        }
    }

    private final TestConverter converter = new TestConverter();

    @Test
    void getImage_whenFilePresent_returnsBase64DataUri() throws Exception {
        MockMultipartFile file = new MockMultipartFile("items[0].image", "img.jpg", "image/jpeg", jpegBytes(100, 100));

        String result = converter.testGetImage(Map.of("items[0].image", file), 0, "image");

        assertThat(result).startsWith("data:image/jpeg;base64,");
    }

    @Test
    void getThumbnail_whenFilePresent_returnsBase64DataUri() throws Exception {
        MockMultipartFile file = new MockMultipartFile("items[0].image", "img.jpg", "image/jpeg", jpegBytes(100, 100));

        String result = converter.testGetThumbnail(Map.of("items[0].image", file), 0, "image");

        assertThat(result).startsWith("data:image/jpeg;base64,");
    }

    @Test
    void getImage_whenFileIsEmpty_returnsNull() {
        MockMultipartFile file = new MockMultipartFile("items[0].image", new byte[0]);

        String result = converter.testGetImage(Map.of("items[0].image", file), 0, "image");

        assertThat(result).isNull();
    }

    @Test
    void getImage_whenKeyNotPresent_returnsNull() {
        String result = converter.testGetImage(Map.of(), 0, "image");

        assertThat(result).isNull();
    }

    @Test
    void getImage_scalesDownLargeImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("items[0].image", "img.jpg", "image/jpeg", jpegBytes(3000, 2000));

        String result = converter.testGetImage(Map.of("items[0].image", file), 0, "image");

        assertThat(result).startsWith("data:image/jpeg;base64,");
    }

    @Test
    void getThumbnail_scalesDownLargeImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("items[0].image", "img.jpg", "image/jpeg", jpegBytes(3000, 2000));

        String result = converter.testGetThumbnail(Map.of("items[0].image", file), 0, "image");

        assertThat(result).startsWith("data:image/jpeg;base64,");
    }

    @Test
    void getImage_whenBytesAreInvalid_throwsIllegalStateException() {
        MockMultipartFile file = new MockMultipartFile("items[0].image", "img.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> converter.testGetImage(Map.of("items[0].image", file), 0, "image"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Image compression failed.");
    }

    private byte[] jpegBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", out);
        return out.toByteArray();
    }
}
