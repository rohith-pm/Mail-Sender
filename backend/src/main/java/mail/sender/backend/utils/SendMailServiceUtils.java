package mail.sender.backend.utils;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest;
import com.google.api.client.json.GenericJson;
import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SendMailServiceUtils {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SendMailServiceUtils.class);

    /**
     * Retry mechanism(Exponential backoff) for rate limit exception from API
     *
     * @param request The request to be executed
     * @param className Class in which the error ocured
     * @param methodName name of the methos
     * @param <K> Response type
     * @param <T> Request type
     * @return Response
     */
    public static <K extends GenericJson, T extends AbstractGoogleJsonClientRequest> K execute(T request, String className,
                                                                                               String methodName) {
        SecureRandom randomGenerator = new SecureRandom();
        for (int backoff = 0; backoff < 7; backoff++) {
            try {
                return (K) request.execute();
            } catch (GoogleJsonResponseException e) {
                GoogleJsonError error = e.getDetails();
                if (SendMailServiceUtils.isRequestRateExceeded(error)) {
                    LOG.error("Failed..., Reason : {}, Action : {}, Trial : {}, Time : {}",
                            new Object[]{error.getErrors().get(0).getReason(), "Applying exponential mechanism",
                                    backoff + 1, new Date()});
                    SendMailServiceUtils.exponentialBackoff(backoff, randomGenerator.nextInt(1001));
                }
            } catch (SocketTimeoutException e) {

                LOG.error("Failed..., Reason : {}, Action : {}, Trial : {}, Time : {}",
                        new Object[]{e.getMessage(), "Applying exponential mechanism", backoff + 1, new Date()});
                SendMailServiceUtils.exponentialBackoff(backoff, randomGenerator.nextInt(1001));
            } catch (IOException e) {
                throw new RuntimeException("IO Exception in " + className + " => " + methodName + " while executing " +
                        "exponential mechanism " + e);
            }
        }
        throw new RuntimeException("Exception in " + className + " => " + methodName + " while executing exponential " +
                "mechanism");
    }

    /**
     * Converts List<MultipartFile> to List<File>
     *
     * @param files List<MultipartFile> files
     * @return List<File>
     */
    public static List<File> convert(List<MultipartFile> files) {
        return (files.stream().map(file -> {
            File convFile = new File(file.getOriginalFilename());
            try {
                convFile.createNewFile();
                FileOutputStream fos = null;
                fos = new FileOutputStream(convFile);
                fos.write(file.getBytes());
                fos.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            return convFile;
        }).collect(Collectors.toList()));
    }

    /**
     * Waits before next request
     *
     * @param n nth retry
     * @param offset random integer
     */
    public static void exponentialBackoff(int n, int offset) {
        // Applying exponential backoff.
        try {
            Thread.sleep((1 << n) * 1000 + offset);
        } catch (InterruptedException e1) {
            throw new RuntimeException("Exponential backoff thread interrupted");
        }
    }

    /**
     *
     * @param error GoogleJsonError object
     * @return true if status code is 429; otherwise false
     */
    public static boolean isRequestRateExceeded(GoogleJsonError error) {
        return error.getCode() == 429;
    }

}
