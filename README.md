# Plataforma de Servicios de Integración

## Descripción

La **API de Manejo de Transacciones Financieras de Pago** es una solución diseñada para gestionar transacciones financieras de diferentes tipos, incluyendo pagos con tarjeta, transferencias bancarias y transferencias P2P. La API está construida utilizando Spring Boot y proporciona endpoints RESTful para crear, consultar y listar transacciones.

## Diagrama de Arquitectura
El diagrama se encuentra disponible en: https://app.eraser.io/workspace/OtAAwV4ZxGQrWNuZj8Ce?origin=share

## Colección de Postman

Para ejecutar los endpoints públicos de la API **se debe utilizar la colección de Postman** que se encuentra dentro del proyecto.
  #### Dentro de la colección se encuentran disponible tres (3) requests para crear Transacciones y dos para ejecutar los dos métodos faltantes.

## Acerca de la API

Esta fue diseñada con las siguientes consideraciones técnicas en mente:
- **Escalabilidad y Rendimiento**: Soporta grandes volúmenes de transacciones de manera eficiente utilizando paginación y especificaciones dinámicas.
- **Transaccionalidad e Idempotencia**: Implementa estrategias para manejar transacciones duplicadas y asegurar la consistencia en un sistema distribuido.
- **Soporte Multimoneda**: Maneja múltiples monedas y convierte los montos a pesos argentinos (ARS) cuando es necesario.
- **Componentes Reutilizables**: Utiliza estructuras y patrones que permiten la integración de nuevos métodos de pago sin refactorización significativa.

## Endpoints Públicos

### `POST /api/v1/transactions`

Crea una nueva transacción basada en el tipo de transacción proporcionado en el cuerpo de la solicitud.

- **Parámetros**:
    - `requestMap` (Map<String, Object>): Mapa que contiene los detalles de la transacción.
- **Respuesta**:
    - `200 OK`: Transacción creada exitosamente.
    - `400 Bad Request`: Error en la creación de la transacción.
    - `500 Internal Server Error`: Error interno del servidor.

### `GET /api/v1/transactions/{transactionId}`

Obtiene el estado de una transacción basada en el ID de la transacción proporcionado.

- **Parámetros**:
    - `transactionId` (String): ID de la transacción.
- **Respuesta**:
    - `200 OK`: Estado de la transacción.
    - `404 Not Found`: Transacción no encontrada.
    - `500 Internal Server Error`: Error interno del servidor.

### `GET /api/v1/transactions`

Lista las transacciones de un usuario basado en los parámetros proporcionados.

- **Parámetros**:
    - `userId` (String): ID del usuario.
    - `status` (String, opcional): Estado de las transacciones a filtrar.
    - `sortBy` (String, opcional, default: "createdAt"): Campo por el cual ordenar.
    - `order` (String, opcional, default: "ASC"): Orden de la clasificación.
    - `pageable` (Pageable): Información de paginación.
- **Respuesta**:
    - `200 OK`: Página de transacciones del usuario.
    - `500 Internal Server Error`: Error interno del servidor.

## Métodos Públicos

### `createTransaction(Map<String, Object> requestMap)`

Crea una nueva transacción basada en el mapa de solicitud proporcionado. Determina el tipo de transacción y llama al manejador apropiado. Convierte el monto a ARS si es necesario.

- **Parámetros**:
    - `requestMap` (Map<String, Object>): Mapa que contiene los detalles de la transacción.
- **Retorno**:
    - Objeto de la transacción creada.
- **Excepciones**:
    - `CustomException`: Si hay un error al crear la transacción.

### `getTransactionStatus(String transactionId)`

Obtiene el estado de una transacción basada en el ID de la transacción proporcionado.

- **Parámetros**:
    - `transactionId` (String): ID de la transacción.
- **Retorno**:
    - Objeto del estado de la transacción.
- **Excepciones**:
    - `CustomException`: Si la transacción no se encuentra.

### `listUserTransactions(String userId, String status, String sortBy, String order, Pageable pageable)`

Lista las transacciones de un usuario basado en los parámetros proporcionados.

- **Parámetros**:
    - `userId` (String): ID del usuario.
    - `status` (String, opcional): Estado de las transacciones a filtrar.
    - `sortBy` (String, opcional, default: "createdAt"): Campo por el cual ordenar.
    - `order` (String, opcional, default: "ASC"): Orden de la clasificación.
    - `pageable` (Pageable): Información de paginación.
- **Retorno**:
    - Página de transacciones del usuario.

## Instalación

1. Clona el repositorio:
   ```bash
   git clone https://github.com/FValenteUADE/Finantial-Transaction-Management-API.git

2. Navega al directorio del proyecto:
    ```bash
    cd Finantial-Transaction-Management-API

3. Abre el proyecto desde IntelliJ / VisualStudio Code / o similar.
4. Inicia / Ejecuta el proyecto.
## Ingreso a la Base de Datos H2

1. Ingresamos al sitio  
    
    #### http://localhost:8080/h2-console
2. En la variable "JDBC URL":
    #### jdbc:h2:mem:testdb
3. En la variable "User Name":
    #### sa
4. En la variable "Password" no ingresamos nada.