# 1. Развертывание
    helm dependency update
    helm -n auth upgrade --install --create-namespace app .
Манифесты развертываются в namespace auth

# 2. Проверка
## Выполнить Postman Collection для приведенного выше сценария
    newman run --insecure Streaming.postman_collection.json

# 3. Варианты решения
## HTTP
![Схема](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/pav37/stream-processing-homework/main/HTTP/HTTP.puml)
## Events
![Схема](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/pav37/stream-processing-homework/main/Events/Events.puml)
## Event Collaboration
![Схема](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/pav37/stream-processing-homework/main/Event_Collaboration/Event_Collaboration.puml)

# 4. Решение
Для реализации решения выбран вариант Event Collaboration. 
Данный вариант позволяет не привязывать сервис аутентификации/регистрации к остальным сервисам, от него требуется только отправить сообщение о регистрации.
Такой стиль взаимодействия позволит в дальнейшем относительно легко добавить другие сервисы, которым требуется информации об этих событиях, к примеру сработка какой-либо автоматики.
Также немаловажным является вразумительное содержание сообщений, поскольку избавляет от необходимости делать дополнительные запросы на получение информации. 
Данный пункт является актуальным также потому, что в дальнейшем отправка команд может производиться автоматически при возникновении нежелательных событий, и таких событий/команд может быть большое количество.

## Список компонентов:
1. Api Gateway (Nginx Controller + Oauth2 proxy) - аутентификация пользователей для доступа к приложению Myapp.
2. Auth Service - сервер аутентификации/авторизации/регистрации пользователей.
3. Oauth2-proxy позволяет использовать внешний сервис аутентификации/авторизации (Auth Service) для доступа к защищенным ресурсам.
4. Command Service - сервис для отправки команд на устройство.
5. Device Management Service - сервис управления устройствами. Содержит список устройств и их сенсоров. 
Каждое устройство может содержать несколько сенсоров - каждому из них соответствует один параметр, который он измеряет (temperature, humidity и тд).
Также сервис хранит список пользователей и групп, которым разрешен доступ к определенным группам устройств.
6. Notification Service - сервис уведомлений. Занимается отправкой уведомлений пользователям при возникновении каких-либо событий. Также хранит список уведомлений в своей БД.

## Сценарий использования (описан в Postman Collection)
1. Пользователь регистрируется в **Auth Service**. 
Auth Service отправляет событие о создании пользователя, на которое подписан **Device Management Service**. 
**Device Management Service** создает аккаунт пользователя с возможностью создания тестовых устройств и управления ими.
2. Пользователь делает login.
3. Пользователь создает в **Device Management Service** устройство с сенсором.
4. Пользователь отправляет команду на установку **корректного** значения параметра temperature для сенсора устройства.
5. Приходит уведомление от сервиса Notification Service, что команда выполнена успешно. В Postman проверяется уведомление в БД событий **Notification Service**.
6. Пользователь отправляет команду на установку **некорректного** значения параметра temperature для сенсора устройства.
7. Приходит уведомление от сервиса Notification Service, что команда не выполнена. В Postman проверяется уведомление в БД событий **Notification Service**.
8. Пользователь делает logout.


# Удаление развернутых ресурсов
    helm -n auth delete app 
    kubectl delete pvc --all -n auth
    kubectl delete pv --all -n auth
    kubectl delete namespace auth

Домашнее задание/проектная работа разработано(-на) для курса [Microservice Architecture](https://otus.ru/lessons/microservice-architecture)
