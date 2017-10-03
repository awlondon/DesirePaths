package com.pdceng.www.desirepaths;

// --Commented out by Inspection START (9/27/2017 9:51 AM):
//class HttpServicesClass {
//    private final ArrayList<NameValuePair> ArrayListParams;
//    private final ArrayList<NameValuePair> headers;
//    private int responseCode;
//    private String message;
//    private String response;
//    private String UrlHolder;
//
//    public HttpServicesClass(String url) {
//        HttpServicesClass.this.UrlHolder = url;
//
//        ArrayListParams = new ArrayList<>();
//
//        headers = new ArrayList<>();
//    }
//
//    public String getResponse() {
//        return response;
//    }
//
//    public String getErrorMessage() {
//        return message;
//    }
//
//    public int getResponseCode() {
//        return responseCode;
//    }
//
//    public void AddParam(String name, String value) {
//        ArrayListParams.add(new BasicNameValuePair(name, value));
//    }
//
//    public void AddHeader(String name, String value) {
//        headers.add(new BasicNameValuePair(name, value));
//    }
//
//    public void ExecuteGetRequest() throws Exception {
//        String MixParams = "";
//
//        if (!ArrayListParams.isEmpty()) {
//            MixParams += "?";
//
//            for (NameValuePair p : ArrayListParams) {
//                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
//
//                if (MixParams.length() > 1) {
//                    MixParams += "&" + paramString;
//                } else {
//                    MixParams += paramString;
//                }
//            }
//        }
//
//        HttpGet httpGet = new HttpGet(UrlHolder + MixParams);
//
//        for (NameValuePair h : headers) {
//            httpGet.addHeader(h.getName(), h.getValue());
//        }
//
//        executeRequest(httpGet, UrlHolder);
//    }
//
//    public void ExecutePostRequest() throws Exception {
//        HttpPost httpPost = new HttpPost(UrlHolder);
//        for (NameValuePair h : headers) {
//            httpPost.addHeader(h.getName(), h.getValue());
//        }
//
//        if (!ArrayListParams.isEmpty()) {
//            httpPost.setEntity(new UrlEncodedFormEntity(ArrayListParams, HTTP.UTF_8));
//        }
//
//        executeRequest(httpPost, UrlHolder);
//    }
//
//    private void executeRequest(HttpUriRequest request, String url) {
//        HttpParams httpParameters = new BasicHttpParams();
//
//        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
//
//        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
//
//        HttpClient httpClient = new DefaultHttpClient(httpParameters);
//
//        HttpResponse httpResponse;
//        try {
//            httpResponse = httpClient.execute(request);
//            responseCode = httpResponse.getStatusLine().getStatusCode();
//            message = httpResponse.getStatusLine().getReasonPhrase();
//
//            HttpEntity entity = httpResponse.getEntity();
//            if (entity != null) {
//                InputStream inputStream = entity.getContent();
//
//                response = convertStreamToString(inputStream);
//
//                inputStream.close();
//            }
//        } catch (IOException e) {
//            httpClient.getConnectionManager().shutdown();
//            e.printStackTrace();
//        }
//    }
//
//    private String convertStreamToString(InputStream is) {
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
//
//        StringBuilder stringBuilder = new StringBuilder();
//
//        String line;
//        try {
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuilder.append(line).append("\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return stringBuilder.toString();
//    }
//
// --Commented out by Inspection STOP (9/27/2017 9:51 AM)
//}