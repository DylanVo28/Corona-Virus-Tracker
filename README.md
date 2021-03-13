Build project a Coronavirus tracker app
B1:Tạo project, các dependency gồm có,spring web,thymeleaf,devtools
B2: Tạo thư mục services theo đường dẫn com.example.coronavirus.services
Folder services: tạo class CoronaVirusDataService, bạn thêm đoạn link ở dưới, đoạn link này sẽ trả về cho chúng ta data(link)
CoronaVirusDataService.java: public class Co…{
	private static String VIRUS_DATA_URL=”https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv”;
	
}

Tiếp tục thêm đoạn code dưới, đoạn code này dùng để tạo 1 client, 1 request data virus, client sẽ gửi request datavrus và sẽ response trả về data mà ta có
CoronaVirusDataService.java: public class Co…{
	...VIRUS_DATA_URL…;
	public void fetchVirusData(){
   HttpClient client= HttpClient.newHttpClient(); 
   HttpRequest request= HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
   HttpResponse<String> httpResponse= client.send(request, HttpResponse.BodyHandlers.ofString());
}
}

Thêm annonation 
@Service(Đánh dấu một Class là tầng Service, phục vụ các logic nghiệp vụ.)
@PostConstruct được đánh dấu trên một method duy nhất bên trong Bean. IoC Container hoặc ApplicationContext sẽ gọi hàm này sau khi một Bean được tạo ra và quản lý.
CoronaVirusDataService.java: 
...
@Service
public class CoronaVirusDataService{
	…,
	@PostConstruct
	public void fetchVirusData...
}



Thêm dependency vào file pom.xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.8</version>
</dependency>



Thêm đoạn code dưới vào file, sẽ trả về các tỉnh trong phạm vi nhiễm 
CoronaVirusDataService.java: 
public Virus…{
…,
public void fetchVirusData(){
	… httpResponse;
StringReader csvBodyReader= new StringReader(httpResponse.body());
Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
for(CSVRecord record : records){
   String state= record.get("Province/State");
   System.out.println(state);
}
}}

Lấy dữ liệu thống kê theo ngày, tham khảo link dưới
https://viblo.asia/p/scheduling-task-trong-spring-boot-yMnKMy2QK7P
Thêm anonation @Scheduled
CoronaVirusDataService.java: 
public CoronaVirusDataService{
	…,
	@PostConstruct
	@Scheduled(cron=”* * 1 * * *”)
	public void fetchVirusData...
}

CoronavirusApplication.java:
...
@SpringBootApplication
@EnableScheduling
public class CoronavirusApplication {...}

Bạn thay đổi lại hàm này
CoronaVirusDataService.java
...VIRUS_DATA_URL,
private List<LocationStats> allStats= new ArrayList<>();

@PostConstruct
@Scheduled(cron = "* * 1 * * *")
public void fetchVirusData() throws IOException, InterruptedException {
   List<LocationStats> newStats= new ArrayList<>();
   …
   for(CSVRecord record : records){
   LocationStats locationStat=new LocationStats();
   locationStat.setState(record.get("Province/State"));
   locationStat.setCountry(record.get("Country/Region"));

locationStat.setLastedTotalCases(Integer.parseInt(record.get(record.size()-1)));
   newStats.add(locationStat);
}
this.allStats=newStats;

}


Tạo thư mục com.example.coronavirus.models
Tạo file LocationStats trong mục models
copy code này vào, đoạn code này khai báo các thuộc tính state,country, tổng số ca nhiễm đến nay
LocationStats.java:

public class LocationStats {
   private String state;
   private String country;
   private int lastedTotalCases;

   public String getState() {
       return state;
   }

   public void setState(String state) {
       this.state = state;
   }

   public String getCountry() {
       return country;
   }

   public void setCountry(String country) {
       this.country = country;
   }

   public int getLastedTotalCases() {
       return lastedTotalCases;
   }

   public void setLastedTotalCases(int lastedTotalCases) {
       this.lastedTotalCases = lastedTotalCases;
   }
}


CoronaVirusDataService.java:

...VIRUS_DATA_URL,
private List<LocationStats> allStats= new ArrayList<>();
@PostConstruct
...

Tạo class HomeController trong thư mục controllers, để get api
HomeController.java:
@GetMapping("/")
public String home(Model model){
   model.addAttribute("title","Title Virus");
   return "index";
}

tạo file index.html trong thư mục templates được tạo sẵn
index.html: 
<!DOCTYPE html>

<html xmlns:th="http://www.thymeleaf.org">

<head>
   <title>Corona Virus Tracker</title>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

</head>

<body>

<p th:text="${title}"></p>

</body>

</html>

Sửa lại file HomeController.java để gán các attribute vào file index:
@Autowired
CoronaVirusDataService coronaVirusDataService;

@GetMapping("/")
public String home(Model model){
   model.addAttribute("locationStats",coronaVirusDataService.getAllStats());
   return "index";
}

Thêm hàm getAllstats trong file CoronaVirusDataService.java để lấy tất cả stat:
...allStats,
public List<LocationStats> getAllStats() {
   return allStats;
}
…

Cuối cùng sửa lại file index, đổ dữ liệu ra UI
index.html:
<table>
   <tr>
       <th>State</th>
       <th>Country</th>
       <th>Total case reported</th>
   </tr>
   <tr th:each="locationStat : ${locationStats}">
       <th th:text="${locationStat.state}"></th>
       <th th:text="${locationStat.country}"></th>
       <th th:text="${locationStat.lastedTotalCases}"></th>
   </tr>
</table>

Css cho xịn nhé
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">

<table style="text-align: left"  class="table table-dark">
   <tr>
       <th scope="col">State</th>
       <th scope="col">Country</th>
       <th scope="col">Total case reported</th>
   </tr>
   <tr th:each="locationStat : ${locationStats}">
       <th th:text="${locationStat.state}"></th>
       <th th:text="${locationStat.country}"></th>
       <th th:text="${locationStat.lastedTotalCases}"></th>
   </tr>
</table>

Code tổng số ca nhiễm
HomeController.java trong hàm home viết thêm 2 cái này vào
int totalReport=coronaVirusDataService.getAllStats().stream().mapToInt(stat -> stat.getLastedTotalCases()).sum();
model.addAttribute("totalReportedCases",totalReport);

File index.html thêm cái <span th:text="${totalReportedCases}"></span>

code cases thay đổi trong ngày

Thêm thuộc tính difffromprevday vao LocationStat.java:
private int diffFromPrevDay;

public int getDiffFromPrevDay() {
   return diffFromPrevDay;
}

public void setDiffFromPrevDay(int diffFromPrevDay) {
   this.diffFromPrevDay = diffFromPrevDay;
}

CoronaVirusDataService.java: trong hàm fetchVirusData
thêm 3 dòng này
int latestCases= Integer.parseInt(record.get(record.size()-1));
int prevDayCases= Integer.parseInt(record.get(record.size()-2));
locationStat.setDiffFromPrevDay(latestCases-prevDayCases);

index.html: <th th:text="${locationStat.diffFromPrevDay}"></th>

Xong rồiiiiiiiii


![image](https://user-images.githubusercontent.com/67864182/111038398-37eb7400-845b-11eb-8bef-4edbbc3e3146.png)
