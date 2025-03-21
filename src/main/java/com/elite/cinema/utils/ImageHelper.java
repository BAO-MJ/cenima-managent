package com.elite.cinema.utils;

import java.io.ByteArrayInputStream;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;

public class ImageHelper {
    private ImageHelper() {}

    public static void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w;
            double h;

            double ratioX = img.getWidth() / imageView.getFitWidth();
            double ratioY = img.getHeight() / imageView.getFitHeight();

            double zoomCoeff = Math.min(ratioX, ratioY);

            w = imageView.getFitWidth() * zoomCoeff;
            h = imageView.getFitHeight() * zoomCoeff;

            imageView.setViewport(new Rectangle2D(img.getWidth() / 2 - w / 2, img.getHeight() / 2 - h / 2, w, h));
        }
    }

    @SneakyThrows
    public static Image byteArrayToImage(byte[] bytes) {
        try (var stream = new ByteArrayInputStream(bytes)) {
            return new Image(stream, 0, 0, false, true);
        }
    }
}
