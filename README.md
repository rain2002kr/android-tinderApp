# 2021-0613
 앱 시작 
 
 # 2021-0617
  파이어베이스 이메일 로그인 
  파이에베이스 facebook 로그인 활성화
   1. 파베에 facebook app id 및 정보 입력 
   2. facebook 에 파베 auth 링크 등록 
   3. 안드로이드 앱에 gradle 에 facebook-login 추가 
      mavenCenter 추가
   4. facebook 에 앱 이름 및 정보 등록    
   
   데이터베이스 설정
   1. gradle 추가 : firebase-database-ktx
   2. dataabase.reference 안에 child 로 가져온다. 
    a. json 형식으로, child("데이터") 이안에 데이터 있으면 가져오고, 없으면 하나 만든다. 
    b. update 할땐, mutableMapof<String, Any> 이용하고, updateChildren 을 이용한다.
    c. 값을 가져올땐, 리스너를 통해서 가져온다. 이때 object 달아서 임플리먼트 메소드 시켜야한다. 
   
    카드스택뷰 
    
    # 결과화면

![1](./사진/깃허브.png)
      
  
 
