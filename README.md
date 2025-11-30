
# Ferretería API

Este repositorio contiene la API para la gestión de una tienda de ferretería. El proyecto está desarrollado con **Spring Boot 3.5.8** y **Java 21**. La aplicación expone varios endpoints RESTful para gestionar productos, categorías, y pedidos (Orders). 

## Requisitos

- **Java 21**: Asegúrate de tener instalado Java 21 en tu máquina.
- **Maven**: Utilizado como herramienta de construcción para gestionar dependencias y ejecutar el proyecto.
- **Spring Boot 3.5.8**: El framework principal para construir la aplicación.
- **MySQL**: Base de datos relacional utilizada para persistir los datos.

## Instalación

Sigue estos pasos para poner en marcha el proyecto:

### 1. Clonar el repositorio

Clona el repositorio en tu máquina local:

```bash
git clone https://github.com/tu_usuario/ferreteria-api.git
2. Navegar al directorio del proyecto
Accede al directorio del proyecto recién clonado:

bash
Copy code
cd ferreteria-api
3. Configurar la base de datos MySQL
Crea una base de datos llamada ferreteria (o el nombre que prefieras).

Asegúrate de tener las credenciales correctas para conectar a MySQL.

4. Configurar la conexión a la base de datos
Abre el archivo src/main/resources/application.properties y reemplaza las credenciales de conexión a la base de datos MySQL:

properties
Copy code
spring.datasource.url=jdbc:mysql://localhost:3306/ferreteria
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.show-sql=true
5. Construir el proyecto con Maven
Construye el proyecto utilizando Maven:

bash
Copy code
mvn clean install
6. Ejecutar el proyecto
Para iniciar el servidor, usa el siguiente comando:

bash
Copy code
mvn spring-boot:run
El servidor se ejecutará en http://localhost:8080 por defecto.

Endpoints de la API
1. Productos (Products)
Crear Producto
POST /products

Crea un nuevo producto en la base de datos.

Body:
json
Copy code
{
  "name": "Caño Fusion",
  "description": "4 metros",
  "img": "https://example.com/image.jpg",
  "price": 5000,
  "category": { "id": 1 },
  "state": true
}
Obtener Todos los Productos
GET /products

Devuelve una lista de todos los productos.

Obtener Producto por ID
GET /products/{id}

Obtiene un producto por su ID.

Actualizar Producto
PUT /products/{id}

Actualiza un producto existente.

Eliminar Producto
DELETE /products/{id}

Elimina un producto por su ID.

2. Categorías (Categories)
Crear Categoría
POST /category

Crea una nueva categoría.

Body:
json
Copy code
{
  "name": "Herramientas"
}
Obtener Todas las Categorías
GET /category

Devuelve una lista de todas las categorías.

Obtener Categoría por ID
GET /category/{id}

Obtiene una categoría por su ID.

Actualizar Categoría
PUT /category/{id}

Actualiza una categoría existente.

Eliminar Categoría
DELETE /category/{id}

Elimina una categoría por su ID.

3. Pedidos (Orders)
Crear Pedido
POST /orders

Crea un nuevo pedido.

Body:
json
Copy code
{
  "customerId": 1,
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
Obtener Todos los Pedidos
GET /orders

Devuelve una lista de todos los pedidos.

Obtener Pedido por ID
GET /orders/{id}

Obtiene un pedido por su ID.

Actualizar Pedido
PUT /orders/{id}

Actualiza un pedido existente.

Eliminar Pedido
DELETE /orders/{id}

Elimina un pedido por su ID.

4. OrderItems (Items de Pedido)
Crear Item de Pedido
POST /order-items

Crea un nuevo ítem de pedido.

Body:
json
Copy code
{
  "orderId": 1,
  "productId": 1,
  "quantity": 2
}
Eliminar Item de Pedido
DELETE /order-items/{id}

Elimina un item de pedido por su ID.

Estructura del Proyecto
El proyecto está organizado de la siguiente manera:

css
Copy code
src/
 ├── main/
 │    ├── java/
 │    │   ├── com/
 │    │   │   └── ferreteria_edu/
 │    │   │       ├── ferreteria_api/
 │    │   │       │   ├── controller/
 │    │   │       │   ├── dto/
 │    │   │       │   ├── exception/
 │    │   │       │   ├── mapper/
 │    │   │       │   ├── model/
 │    │   │       │   ├── repository/
 │    │   │       │   └── service/
 │    │   │       └── ...
 └── resources/
      └── application.properties
Descripción de las carpetas principales:
controller/: Contiene los controladores REST que manejan las solicitudes HTTP.

dto/: Contiene los objetos de transferencia de datos (DTO), utilizados para la entrada y salida de datos en la API.

exception/: Contiene clases para manejar excepciones personalizadas.

mapper/: Contiene las clases de mapeo entre entidades y DTOs.

model/: Contiene las entidades del dominio (como Product, Category, Order, etc.).

repository/: Contiene los repositorios que interactúan con la base de datos.

service/: Contiene la lógica de negocio y servicios relacionados con las entidades.

Dependencias
El proyecto usa las siguientes dependencias principales:

Spring Boot 3.5.8: Framework principal.

Spring Data JPA: Para la interacción con la base de datos.

MySQL: Base de datos relacional.

Lombok: Para reducir el código repetitivo.

ModelMapper: Para facilitar la conversión entre entidades y DTOs.

Tecnologías Utilizadas
Java 21

Spring Boot 3.5.8

Maven

JPA (Hibernate)

MySQL

Lombok

ModelMapper

Licencia
Este proyecto está licenciado bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

markdown
Copy code

### Instrucciones:
1. Copia el contenido de este archivo tal cual.
2. Pega el contenido en un archivo llamado `README.md` en el directorio raíz de tu repositorio de GitHub.

Esto debería proporcionarte una documentación completa que explica cómo configurar, ejecutar y contribuir al proyecto.



Attach

Search

Study

Voice

ChatGPT can ma
