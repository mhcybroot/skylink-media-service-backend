Update the FileStorageService workflow:

    When a file is uploaded, use the metadata-extractor library to get the GPS, Make/Model, and Timestamp other all info dynamically store everything.

    Save these details into our project_photos table in PostgreSQL.

    After saving to the DB, run the cwebp optimization with -metadata none to strip the info from the file for security.

    Return the database record ID to the frontend.
    on frontend admin deshboard project image card add a metadata for each image