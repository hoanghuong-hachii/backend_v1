package com.api19_4.api19_4.controller;

import com.api19_4.api19_4.models.*;
import com.api19_4.api19_4.models.ImageProductAvatar;
import com.api19_4.api19_4.repositories.ImageRepositoryAvatar;
import com.api19_4.api19_4.repositories.ImageRepositoryDetail;
import com.api19_4.api19_4.services.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping(path = "/api/v2/FileUpload")
public class FileUploadController {
//    this controller receive file/image from client
//    Inject Storage Service here
    @Autowired
    private IStorageService storageService;
    @Autowired
    private ImageRepositoryAvatar repository;
    @Autowired
    private ImageRepositoryDetail repositoryDetail;

    //post image_Avatar, image_detail
    @PostMapping("/uploadAvatar")
    public ResponseEntity<ResponseObject> uploadFilesAvatar(
            @RequestParam("imageAvatar") MultipartFile imageAvatar,
            @RequestParam("idProd") Long idProd,
            @RequestParam("imageName") String imageName) {
        try {
            // Kiểm tra và lưu trữ imageAvatar
            String generatedImageAvatarFileName = storageService.storeFile(imageAvatar);
            // Thực hiện các thao tác khác, ví dụ: lưu các thông tin khác vào cơ sở dữ liệu


            // Tạo đối tượng Image với các thuộc tính đã nhận được
            ImageProductAvatar image = new ImageProductAvatar();
            image.setIdProd(idProd);
            image.setImageName(imageName);
            image.setImageAvatar(generatedImageAvatarFileName);

            // Lưu đối tượng Image vào cơ sở dữ liệu (hoặc thực hiện thao tác tùy ý)

            // Tạo đối tượng ResponseObject để trả về
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Upload files and save Image successfully",repository.save(image) )
            );
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject("error", exception.getMessage(), null)
            );
        }
    }

    @PostMapping("/uploadDetail")
    public ResponseEntity<ResponseObject> uploadFilesDetail(
            @RequestParam("imageDetail") MultipartFile[] imageDetails,
            @RequestParam("idProd") Long idProd,
            @RequestParam("imageName") String imageName) {
        try {
            List<String> generatedImageAvatarFileNames = new ArrayList<>();

            for (MultipartFile imageDetail : imageDetails) {
                // Kiểm tra và lưu trữ imageDetail
                String generatedImageAvatarFileName = storageService.storeFile(imageDetail);
                generatedImageAvatarFileNames.add(generatedImageAvatarFileName);
            }

            // Tạo đối tượng Image với các thuộc tính đã nhận được
            ImageProductDetail image = new ImageProductDetail();
            image.setIdProd(idProd);
            image.setImageName(imageName);
            image.setImageDetail(generatedImageAvatarFileNames);

            // Lưu đối tượng Image vào cơ sở dữ liệu (hoặc thực hiện thao tác tùy ý)
            ImageProductDetail savedImage = repositoryDetail.save(image);

            // Tạo đối tượng ResponseObject để trả về
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Upload files and save ImageDetail successfully", savedImage)
            );
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject("error", exception.getMessage(), null)
            );
        }
    }







    //    @PostMapping("/uploadDetail")
//    public ResponseEntity<ResponseObject> uploadFilesDetail(
//            @RequestParam("imageDetail") MultipartFile imageDetail,
//            @RequestParam("idProd") Long idProd,
//            @RequestParam("imageName") String imageName) {
//        try {
//            // Kiểm tra và lưu trữ imageDetail
//            String generatedImageAvatarFileName = storageService.storeFile(imageDetail);
//            // Thực hiện các thao tác khác, ví dụ: lưu các thông tin khác vào cơ sở dữ liệu
//
//            // Tạo đối tượng Image với các thuộc tính đã nhận được
//            ImageProductDetail image = new ImageProductDetail();
//            image.setIdProd(idProd);
//            image.setImageName(imageName);
//            image.setImageDetail(generatedImageAvatarFileName);
//
//            System.out.println(image.toString());
//
//            // Lưu đối tượng Image vào cơ sở dữ liệu (hoặc thực hiện thao tác tùy ý)
//
//            // Tạo đối tượng ResponseObject để trả về
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject("ok", "Upload files and save ImageDetail successfully",repositoryDetail.save(image) )
//            );
//        } catch (Exception exception) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject("error", exception.getMessage(), null)
//            );
//        }
//    }
    // get imageAvatar
    @GetMapping("/imageAvatar/{idProd}")
    public ResponseEntity<byte[]> readDetailFileAvatar(@PathVariable Long idProd) {
        try {
            // Thực hiện logic để tìm và lấy thông tin ảnh dựa trên idProd
            List<ImageProductAvatar> images = repository.findByIdProd(idProd);
            if (images == null || images.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ImageProductAvatar image = images.get(0); // Lấy ảnh đầu tiên trong danh sách

            String fileName = image.getImageAvatar();
            byte[] bytes = storageService.readFileContent(fileName);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(bytes);
        } catch (Exception exception) {
            return ResponseEntity.noContent().build();
        }
    }


//    @GetMapping("/imageDetail/{idProd}")
//    public ResponseEntity<byte[]> readDetailFileDetail(@PathVariable Long idProd) {
//        try {
//            // Thực hiện logic để tìm và lấy thông tin ảnh dựa trên idProd
//            List<ImageProductDetail> images = repositoryDetail.findByIdProd(idProd);
//            if (images == null || images.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            List<byte[]> imageBytesList = new ArrayList<>();
//
//            for (ImageProductDetail image : images) {
//                String[] fileNames = image.getImageDetail().split(",");
//                for (String fileName : fileNames) {
//                    byte[] bytes = storageService.readFileContent(fileName.trim());
//                    imageBytesList.add(bytes);
//                }
//            }
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            for (byte[] imageBytes : imageBytesList) {
//                outputStream.write(imageBytes);
//            }
//
//            byte[] mergedBytes = outputStream.toByteArray();
//
//            return ResponseEntity
//                    .ok()
//                    .contentType(MediaType.IMAGE_JPEG)
//                    .body(mergedBytes);
//        } catch (Exception exception) {
//            return ResponseEntity.noContent().build();
//        }
//    }








//    @PostMapping("")
//
////    @PostMapping("/upload")
//    public ResponseEntity<ResponseObject> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
//        try {
//            List<String> generatedFileNames = new ArrayList<>();
//
//            for (MultipartFile file : files) {
//                String generatedFileName = storageService.storeFile(file);
//                generatedFileNames.add(generatedFileName);
//            }
//
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject("ok", "Upload files successfully", generatedFileNames)
//            );
//        } catch (Exception exception) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new ResponseObject("error", exception.getMessage(), null)
//            );
//        }
//    }



//
//    public ResponseEntity<ResponseObject> uploadFile(@RequestParam("file")MultipartFile file){
//        try{
////      save files to a folder => user a service
//            String generatedFileName = storageService.storeFile(file);
//            return ResponseEntity.status(HttpStatus.OK).body(
//                    new ResponseObject("ok", "upload file successfully", generatedFileName)
//            );
//        }catch (Exception exception){
//            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
//                    new ResponseObject("ok", exception.getMessage(), "")
//            );
//
//        }
//    }

    //get image's url
//    @GetMapping("/files/{fileName:.+}")
//  //  @GetMapping("/uploadAll/{id}")
//    public ResponseEntity<byte[]> readDetailFile(@PathVariable String fileName){
//        try {
//            byte[] bytes = storageService.readFileContent(fileName);
//            return ResponseEntity
//                    .ok()
//                    .contentType(MediaType.IMAGE_JPEG)
//                    .body(bytes);
//        }catch (Exception exception){
//            return ResponseEntity.noContent().build();
//        }
//    }

    // hien thị anh dua tren idProd
//    @GetMapping("/{idProd}")
//    public ResponseEntity<byte[]> readDetailFile(@PathVariable Long idProd) {
//        try {
//            // Thực hiện logic để tìm và lấy thông tin ảnh dựa trên idProd
//            // Ví dụ:
//            List<ImageProduct> images = repository.findByIdProd(idProd);
//            if (images == null || images.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            ImageProduct image = images.get(0); // Lấy ảnh đầu tiên trong danh sách
//
//            String fileName = image.getImageAvatar();
//            byte[] bytes = storageService.readFileContent(fileName);
//
//            return ResponseEntity
//                    .ok()
//                    .contentType(MediaType.IMAGE_JPEG)
//                    .body(bytes);
//        } catch (Exception exception) {
//            return ResponseEntity.noContent().build();
//        }
//    }

    // hien thi anh theo idprod  dạng list url
//    @GetMapping("/{idProd}")
//    public ResponseEntity<byte[]> readDetailFileAvatar(@PathVariable Long idProd) {
//        try {
//            // Thực hiện logic để tìm và lấy thông tin ảnh dựa trên idProd
//            // Ví dụ:
//            List<ImageProduct> images = repository.findByIdProd(idProd);
//            if (images == null || images.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            ImageProduct image = images.get(0); // Lấy ảnh đầu tiên trong danh sách
//
//            String fileName = image.getImageAvatar();
//            byte[] bytes = storageService.readFileContent(fileName);
//
//            return ResponseEntity
//                    .ok()
//                    .contentType(MediaType.IMAGE_JPEG)
//                    .body(bytes);
//        } catch (Exception exception) {
//            return ResponseEntity.noContent().build();
//        }
//    }






//    load all uploaded files
//    @GetMapping("")
//    public ResponseEntity<ResponseObject> getUploadedFiles(){
//        try {
//            List<String> urls = storageService.loadAll()
//                    .map(path ->{
////                        convert filename to url (send request "readDetailFile)
//                        String urlPath = MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                                "readDetailFile", path.getFileName().toString()).build().toUri().toString();
//                        return urlPath;
//                    })
//                    .collect(Collectors.toList());
//            return ResponseEntity.ok(new ResponseObject("ok", "List files successfully", urls));
//        }catch (Exception exception){
//            return ResponseEntity.ok(new ResponseObject("failed", "List files failed", new String[] {}));
//        }
//    }

//    @GetMapping("")
//    public ResponseEntity<ResponseObject> getUploadedFiles(){
//        try {
//            List<String> urls = storageService.loadAll()
//                    .map(path -> {
//                        // Xây dựng URL cho ảnh dựa trên đường dẫn cơ bản và tên tệp
//                        String baseUrl = "http://localhost:8080/api/v2/FileUpload/";
//                        String urlPath = baseUrl + path.getFileName().toString();
//                        return urlPath;
//                    })
//                    .collect(Collectors.toList());
//            return ResponseEntity.ok(new ResponseObject("ok", "List files successfully", urls));
//        } catch (Exception exception) {
//            return ResponseEntity.ok(new ResponseObject("failed", "List files failed", new String[] {}));
//        }
//    }






//    @GetMapping("/{idProd}")
//    public ResponseEntity<byte[]> readDetailFile(@PathVariable Long idProd) {
//        try {
//            // Thực hiện logic để tìm và lấy thông tin ảnh dựa trên idProd
//            List<ImageProduct> images = repository.findByIdProd(idProd);
//            if (images == null || images.isEmpty()) {
//                return ResponseEntity.notFound().build();
//            }
//
//            ImageProduct image = images.get(0); // Lấy ảnh đầu tiên trong danh sách
//
//            String fileName = image.getImageAvatar();
//            try {
//                byte[] bytes = storageService.readFileContent(fileName);
//                return ResponseEntity
//                        .ok()
//                        .contentType(MediaType.IMAGE_JPEG)
//                        .body(bytes);
//            } catch (Exception exception) {
//                return ResponseEntity.noContent().build();
//            }
//        } catch (Exception exception) {
//            return ResponseEntity.noContent().build();
//        }
//    }




}
