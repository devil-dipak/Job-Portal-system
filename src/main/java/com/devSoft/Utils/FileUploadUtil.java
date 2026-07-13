package com.devSoft.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadUtil {

    public static String saveFile(String dir, String fileName, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(dir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String uniqueName = System.currentTimeMillis() + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return uniqueName;
    }

    public static void deleteFile(String dir, String fileName) {
        try {
            Path filePath = Paths.get(dir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FileItem> listFiles(String dir) {
        List<FileItem> items = new ArrayList<>();
        File folder = new File(dir);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        FileItem item = new FileItem();
                        item.setName(f.getName());
                        item.setSize(formatSize(f.length()));
                        item.setImage(isImageFile(f.getName()));
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    private static boolean isImageFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".gif")
                || lower.endsWith(".bmp") || lower.endsWith(".svg")
                || lower.endsWith(".webp");
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    public static class FileItem {
        private String name;
        private String size;
        private boolean isImage;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
        public boolean isImage() { return isImage; }
        public void setImage(boolean image) { isImage = image; }
    }
}
