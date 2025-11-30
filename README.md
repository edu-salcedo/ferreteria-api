# 🔩 Ferretería API
Una API RESTful desarrollada con Spring Boot para la gestión de productos, categorías y pedidos de una ferretería.

# 🛠️ Tecnologías Utilizadas

Este proyecto utiliza un conjunto de tecnologías modernas para proporcionar una solución robusta y escalable:
- Java 21
- Spring Boot 3.5.8
- MavenJPA (Hibernate)
- MySQL
- Lombok
- ModelMapper
- 
## 🚀 Instalación y Ejecución

Sigue estos pasos para configurar y ejecutar el proyecto en tu entorno local.
- 1. Clonar el Repositorio
     Abre tu terminal o línea de comandos y ejecuta:Bashgit clone https://github.com/tu_usuario/ferreteria-api.git
     
- 2. Navegar al Directorio del Proyecto
   Accede al directorio del proyecto recién clonado:
Bashcd ferreteria-api
- 3. Configurar la Base de Datos MySQL
   Asegúrate de tener un servidor MySQL en ejecución.
   Crea una base de datos llamada ferreteria (o el nombre que prefieras).
   Asegúrate de tener las credenciales correctas para conectar a MySQL.
- 4. Configurar la Conexión a la Base de Datos
     Abre el archivo de configuración src/main/resources/application.properties y reemplaza las credenciales de conexión con las de tu base de datos:
     Properties
 spring.datasource.url=jdbc:mysql://localhost:3306/ferreteria
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.show-sql=true
- 5. Construir el Proyecto con Maven
     Construye el proyecto utilizando Maven:
     Bashmvn
      clean install
6. Ejecutar el Proyecto
   Para iniciar el servidor Spring Boot, usa el siguiente comando:
   Bashmvn
   spring-boot:run
El servidor se ejecutará en http://localhost:8080 por defecto.
## 🔌 Endpoints de la API
La API expone los siguientes endpoints para la gestión de recursos:
1. Productos (/products)
   Método,Endpoint,Descripción,Cuerpo de Solicitud (Ejemplo)
POST,/products,Crea un nuevo producto.,"json\n{\n  ""name"": ""Caño Fusion"",\n  ""description"": ""4 metros"",\n  ""img"": ""https://example.com/image.jpg"",\n  ""price"": 5000,\n  ""category"": { ""id"": 1 },\n  ""state"": true\n}"
GET,/products,Obtiene una lista de todos los productos.,N/A
GET,/products/{id},Obtiene un producto por su ID.,N/A
PUT,/products/{id},Actualiza un producto existente.,Mismo cuerpo que POST
DELETE,/products/{id},Elimina un producto por su ID.,N/A
   
   Categorías (/category)
  Método,Endpoint,Descripción,Cuerpo de Solicitud (Ejemplo)
POST,/category,Crea una nueva categoría.,"json\n{\n  ""name"": ""Herramientas""\n}"
GET,/category,Obtiene una lista de todas las categorías.,N/A
GET,/category/{id},Obtiene una categoría por su ID.,N/A
PUT,/category/{id},Actualiza una categoría existente.,Mismo cuerpo que POST
DELETE,/category/{id},Elimina una categoría por su ID.,N/A

    Pedidos (/orders)
 Método,Endpoint,Descripción,Cuerpo de Solicitud (Ejemplo)
POST,/orders,Crea un nuevo pedido.,"json\n{\n  ""customerId"": 1,\n  ""orderItems"": [\n    { ""productId"": 1, ""quantity"": 2 },\n    { ""productId"": 2, ""quantity"": 1 }\n  ]\n}"
GET,/orders,Devuelve una lista de todos los pedidos.,N/A
GET,/orders/{id},Obtiene un pedido por su ID.,N/A
PUT,/orders/{id},Actualiza un pedido existente.,Mismo cuerpo que POST
DELETE,/orders/{id},Elimina un pedido por su ID.,N/A
   
## 📂 Estructura del ProyectoEl proyecto
sigue una estructura modular típica de Spring Boot organizada por capas:CSS
src/
 ├── main/
 │    ├── java/
 │    │   ├── com/
 │    │   │   └── ferreteria_edu/
 │    │   │       └── ferreteria_api/
 │    │   │           ├── controller/
 │    │   │           ├── dto/
 │    │   │           ├── exception/
 │    │   │           ├── mapper/
 │    │   │           ├── model/
 │    │   │           ├── repository/
 │    │   │           └── service/
 │    └── resources/
 │        └── application.properties
Descripción de las Carpetas Principales:

controller/: Contiene los controladores REST que manejan las solicitudes HTTP.

dto/: Contiene los Objetos de Transferencia de Datos (DTO), utilizados para la entrada y salida de datos en la API.

exception/: Contiene clases para manejar excepciones personalizadas.

mapper/: Contiene las clases de mapeo entre entidades y DTOs, utilizando ModelMapper.

model/: Contiene las entidades del dominio (como Product, Category, Order, etc.).

repository/: Contiene los repositorios que interactúan con la base de datos, usando Spring Data JPA.

service/: Contiene la lógica de negocio y servicios relacionados con las entidades.
