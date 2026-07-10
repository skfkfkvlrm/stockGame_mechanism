import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.*;

public class RefactorDTO {
    private static final String BASE_PACKAGE = "com.skfkfkvlrm.stockgame_spring";
    private static final String BASE_DIR = "src/main/java/com/skfkfkvlrm/stockgame_spring";
    
    public static void main(String[] args) throws IOException {
        Path startPath = Paths.get(BASE_DIR, "controller", "dto");
        if (!Files.exists(startPath)) return;
        
        List<Path> javaFiles = new ArrayList<>();
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".java")) {
                    javaFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        Map<String, String> classToNewPackageMap = new HashMap<>();
        Map<Path, Path> fileMoveMap = new HashMap<>();

        for (Path file : javaFiles) {
            String fileName = file.getFileName().toString().replace(".java", "");
            String lowerName = fileName.toLowerCase();
            String targetDomain = "common";
            
            if (lowerName.contains("member") || lowerName.contains("student") || lowerName.contains("login") || lowerName.contains("join")) {
                targetDomain = "member";
            } else if (lowerName.contains("stock") || lowerName.contains("order")) {
                targetDomain = "stock";
            } else if (lowerName.contains("point") || lowerName.contains("asset") || lowerName.contains("transaction") || lowerName.contains("getpoint") || lowerName.contains("history")) {
                targetDomain = "point";
            } else if (lowerName.contains("coupon")) {
                targetDomain = "coupon";
            } else if (lowerName.contains("news")) {
                targetDomain = "news";
            } else if (lowerName.contains("admin") || lowerName.contains("marketsettings")) {
                targetDomain = "admin";
            } else if (lowerName.contains("ai")) {
                targetDomain = "ai";
            }

            String newPackage = BASE_PACKAGE + ".domain." + targetDomain + ".dto";
            classToNewPackageMap.put(fileName, newPackage);
            
            Path newFilePath = Paths.get(BASE_DIR, "domain", targetDomain, "dto", fileName + ".java");
            fileMoveMap.put(file, newFilePath);
        }

        for (Map.Entry<Path, Path> entry : fileMoveMap.entrySet()) {
            Path oldFile = entry.getKey();
            Path newFile = entry.getValue();
            
            Files.createDirectories(newFile.getParent());
            String content = new String(Files.readAllBytes(oldFile));
            String fileName = oldFile.getFileName().toString().replace(".java", "");
            String newPackage = classToNewPackageMap.get(fileName);
            
            content = content.replaceAll("package\\s+com\\.skfkfkvlrm\\.stockgame_spring\\.[a-zA-Z0-9_.]+;", "package " + newPackage + ";");
            
            Files.write(newFile, content.getBytes());
            Files.delete(oldFile);
        }

        // Update all imports in ALL java files (including tests)
        List<Path> allJavaFiles = new ArrayList<>();
        Files.walkFileTree(Paths.get("src"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".java")) {
                    allJavaFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        for (Path file : allJavaFiles) {
            String content = new String(Files.readAllBytes(file));
            boolean changed = false;
            
            for (Map.Entry<String, String> mapping : classToNewPackageMap.entrySet()) {
                String className = mapping.getKey();
                String newPkg = mapping.getValue();
                
                Pattern p = Pattern.compile("import\\s+com\\.skfkfkvlrm\\.stockgame_spring\\.[a-zA-Z0-9_.]+\\." + className + ";");
                Matcher m = p.matcher(content);
                if (m.find()) {
                    content = m.replaceAll("import " + newPkg + "." + className + ";");
                    changed = true;
                }
            }
            if (changed) {
                Files.write(file, content.getBytes());
            }
        }
    }
}
