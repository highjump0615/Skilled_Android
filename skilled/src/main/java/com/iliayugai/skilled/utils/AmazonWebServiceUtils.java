package com.iliayugai.skilled.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

import java.net.URL;

public class AmazonWebServiceUtils {

    private static final String TAG = AmazonWebServiceUtils.class.getSimpleName();

    // Constants used to represent your AWS Credentials.
    private static final String AWS_ACCESS_KEY_ID = "AKIAIXO5Z5NGL5ORVPIA";
    private static final String AWS_ACCESS_SECRET_KEY = "4UsKPi/rEILPvxEhU/+U5YQY0+clTw98mF17XOxW";

    // Constants for the Bucket and Object name.
    private static final String VIDEO_BUCKET = "skilled-v1.1-oregon";
    //private static final String TEST_VIDEO_BUCKET = "skilledv1.1";//"skilld-blog-video";

    public static AmazonS3Client mS3Client;
    public static TransferManager mS3TransferManager;

    static {
        AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_ACCESS_SECRET_KEY);
        mS3Client = new AmazonS3Client(awsCredentials);

        // Please refer http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region
        mS3Client.setEndpoint("s3-us-west-2.amazonaws.com");
        mS3Client.setTimeOffset(3600);

        mS3TransferManager = new TransferManager(mS3Client);
    }

    /*public AmazonWebServiceUtils() {
        //AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
        AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_ACCESS_SECRET_KEY);
        mS3Client = new AmazonS3Client(awsCredentials);
    }*/

    public static URL getFileURL(String objectKey) {
        try {
            java.util.Date expiration = new java.util.Date();
            long milliseconds = expiration.getTime();
            milliseconds += 1000 * 60 * 60; // 1 hour.
            expiration.setTime(milliseconds);

            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            override.setContentType( "video/mp4" );

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(VIDEO_BUCKET, objectKey);
            generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
            generatePresignedUrlRequest.setExpiration(expiration);
            generatePresignedUrlRequest.setResponseHeaders(override);

            URL url = mS3Client.generatePresignedUrl(generatePresignedUrlRequest);
            Log.d(TAG, "Pre-Signed URL = " + url.toString());

            return url;
        } catch (AmazonClientException ace) {
            if (Config.DEBUG) {
                Log.e(TAG, "Caught an AmazonClientException, " +
                        "which means the client encountered " +
                        "an internal error while trying to communicate" +
                        " with S3, such as not being able to access the network.");
                Log.e(TAG, "Error Message: " + ace.getMessage());
            }
        }

        return null;
    }

    // 5MB is the smallest part size allowed for a multi-part upload. (Only the last part can be smaller.)
    private static final int PART_SIZE = (5 * 1024 * 1024);

    public static void uploadVideo(String keyName, String filePath, ProgressListener progressListener) {
        //mS3Client.createBucket(VIDEO_BUCKET);
        PutObjectRequest por = new PutObjectRequest(VIDEO_BUCKET, keyName, new java.io.File(filePath));
        por.withGeneralProgressListener(progressListener);
        mS3Client.putObject(por);

        // Multi-part uploading in high level
        //AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_ACCESS_SECRET_KEY);
        //TransferManager tm = new TransferManager(awsCredentials);

        // TransferManager processes all transfers asynchronously,
        // so this call will return immediately.
        Upload upload = mS3TransferManager.upload(por);

        try {
            // Or you can block and wait for the upload to finish
            upload.waitForCompletion();
            System.out.println("Upload complete.");
        } catch (AmazonClientException amazonClientException) {
            System.out.println("Unable to upload file, upload was aborted.");
            amazonClientException.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Unable to upload file, upload was aborted.");
        }


        //// Low level multi-part uploading
        /*
        String existingBucketName = TEST_VIDEO_BUCKET;

        // Create a list of UploadPartResponse objects. You get one of these for
        // each part upload.
        List<PartETag> partETags = new ArrayList<PartETag>();

        // Step 1: Initialize.
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
                existingBucketName, keyName);
        InitiateMultipartUploadResult initResponse =
                mS3Client.initiateMultipartUpload(initRequest);

        File file = new File(filePath);
        long contentLength = file.length();
        long partSize = PART_SIZE; // Set part size to 5 MB.

        try {
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(PART_SIZE, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(existingBucketName)
                        .withKey(keyName)
                        .withUploadId(initResponse.getUploadId())
                        .withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withGeneralProgressListener(progressListener)
                        .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(mS3Client.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new
                    CompleteMultipartUploadRequest(existingBucketName,
                    keyName,
                    initResponse.getUploadId(),
                    partETags);

            mS3Client.completeMultipartUpload(compRequest);
        } catch (Exception e) {
            mS3Client.abortMultipartUpload(new AbortMultipartUploadRequest(
                    existingBucketName, keyName, initResponse.getUploadId()));
        }
        */
    }

    public static void deleteVideo(Context context, String key) {
        new DeleteVideoTask(context).execute(key);
    }

    private static class DeleteVideoTask extends AsyncTask<String, String, Void> {

        private Dialog mProgressDialog;

        public DeleteVideoTask(Context context) {
            mProgressDialog = CommonUtils.createFullScreenProgress(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(String... params) {
            if (mS3Client != null)
                mS3Client.deleteObject(new DeleteObjectRequest(VIDEO_BUCKET, params[0]));
            return null;
        }
    }

}
