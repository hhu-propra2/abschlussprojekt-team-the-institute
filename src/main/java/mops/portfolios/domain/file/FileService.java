package mops.portfolios.domain.file;

import java.io.IOException;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import mops.portfolios.PortfoliosApplication;
import mops.portfolios.domain.entry.EntryField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class FileService {
  private static final transient Logger logger =
          LoggerFactory.getLogger(PortfoliosApplication.class);

  @Autowired @NonNull
  private transient FileRepository fileRepository;

  /**
   * Checks whether anything is uploaded (whether file has any content).
   * @param file The file to read
   * @return <strong>true</strong> if no content or an error while parsing, \
   * <strong>false</strong> if all's well
   */
  public boolean nothingUploaded(MultipartFile file) {
    byte[] fileBytes = readFile(file);
    if (fileBytes == null) {
      return true;
    }

    return false;
  }

  /**
   * Reads and returns file as byte[].
   * @param file the file to read
   * @return <strong>null</strong> if any IOException while reading, the bytes of the file otherwise
   */
  @SuppressWarnings("PMD")
  public byte[] readFile(MultipartFile file) {
    byte[] fileBytes;

    try {
      fileBytes = file.getBytes();
    } catch (IOException io) {
      logger.warn("Could not read file", io);
      return null;
    }

    return fileBytes;
  }

  public void updateField(MultipartFile file, EntryField field) {
    fileRepository.saveFile(file, field);
  }

  @SuppressWarnings("PMD")
  public String getOriginalFileName(String fileName) {
    if(fileName == null) {
      return null;
    } else {
      String[] names = fileName.split(";");
      if (names.length == 1) {
        return names[0];
      } else{
        return names[1];
      }
    }
  }

  public String getFileUrl(String attachment) {
    return fileRepository.getFileUrl(attachment);
  }

  public Boolean isImage(String fileName) {
    final String[] IMG_EXTENSIONS = {"png", "jpg", "jpeg", "svg", "gif"};
    String last = fileName.substring(fileName.lastIndexOf('.') + 1);

    return Arrays.asList(IMG_EXTENSIONS).contains(last);
  }
}
