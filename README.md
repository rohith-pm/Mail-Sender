Application to send huge number of emails using Gmail API for the purpose of testing. 
![image](https://user-images.githubusercontent.com/40338749/111032669-648b9580-8433-11eb-8637-36f945fb1365.png)

Backend: Spring Boot

Frontend: React JS

Steps to run:

1)Run the frontend using the following commands:
  (i) npm install
  (ii) npm start
  
2)Run the backend application's main method

3)Once both are started, goto https://developers.google.com/gmail/api/quickstart/java and click on enable api > Yes > Next > Desktop Application > Create and download configuration

4)Provide this file for authentication in the React JS application

5)For the first time, you would be prompted to authorize your account by clicking on the link in the backend console. This creates an authentication token and uses it for further requests.
  Ex:Please open the following address in your browser:
  https://accounts.google.com/o/oauth2/auth?access_type=offline........

NOTE:

1)Exponential backoff mechanism is implemented for handling too many requests exceptions (i.e.) for status code 429
![image](https://user-images.githubusercontent.com/40338749/111032628-2b532580-8433-11eb-978a-69f89e888a98.png)

2)Multi-threading implemented for sending many emails quickly.

3)Make sure the attachment size is not above 10 MB


