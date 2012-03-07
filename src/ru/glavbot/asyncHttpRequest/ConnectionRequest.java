package ru.glavbot.asyncHttpRequest;



public class ConnectionRequest  {



		public static final int GET = 0;
		public static final int POST = 1;
		public static final int PUT = 2;
		public static final int DELETE = 3;

		private String url;
		private int method;
		private String data;

		private int timeout = 10000;
		private boolean readAll=true;
		private IProcessAsyncRequestResponse progressProcessor=null;
		private IProcessAsyncRequestResponse answerProcessor=null;
	
		public  ConnectionRequest(int method, String url) {
			this.method=method;
			this.url=url;
			this.data="";
		
		}
		
	
		public  ConnectionRequest(int method, String url, String data) {
			this.method=method;
			this.url=url;
			this.data=data;
		}

	


		public int getTimeout() {
			return timeout;
		}

		public void setTimeout(int timeout) {
			this.timeout = timeout;
		}

		public boolean shouldReadAll() {
			return readAll;
		}

		public void setReadAll(boolean readAll) {
			this.readAll = readAll;
		}




		public String getUrl() {
			return url;
		}




		public void setUrl(String url) {
			this.url = url;
		}




		public int getMethod() {
			return method;
		}




		public void setMethod(int method) {
			this.method = method;
		}




		public String getData() {
			return data;
		}




		public void setData(String data) {
			this.data = data;
		}


		public IProcessAsyncRequestResponse getProgressProcessor() {
			return progressProcessor;
		}


		public void setProgressProcessor(IProcessAsyncRequestResponse responceProcessor) {
			this.progressProcessor = responceProcessor;
		}


		public IProcessAsyncRequestResponse getAnswerProcessor() {
			return answerProcessor;
		}


		public void setAnswerProcessor(IProcessAsyncRequestResponse answerProcessor) {
			this.answerProcessor = answerProcessor;
		}



	
}
