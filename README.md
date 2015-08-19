# Service Provider

## Fonctionnalités générales
Cette librairie est une implémentation du Service Provider compatible à partir de Java 3 
- Facile d'utilisation, il suffit de rajouter la dépendance Maven dans votre application.
- Disponible sur le repository central de Maven.

## Utilisation rapide

- Ajouter la dépendance dans votre projet :

````xml
<dependency>
	<groupId>com.github.marcosemiao.util</groupId>
    <artifactId>service-provider</artifactId>
    <version>1.0.0</version>
</dependency>
````

- Pour récuperer toutes les implémentations d'une interface il suffit de spécifier la classe de l'interface, par exemple avec une interface se nommant "interfaceTest" :

````java
Iterator providers = fr.ms.util.Service.providers(InterfaceTest.class);

while (providers.hasNext()) {
	final InterfaceTest impl = (InterfaceTest) providers.next();
}
````

On récupere un "Iterator" contenant toutes les implémentations trouvées.
