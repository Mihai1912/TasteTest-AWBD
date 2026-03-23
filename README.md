# TasteTest

## Entity Relationship Model (ERM)

Mai jos este schema bazei de date (ERM/ERD) folosită de aplicație.

![image1](./erm.png)

---

## Cerințe funcționale principale

1. **Autentificare și autorizare**
   - Utilizatorii se pot înregistra, autentifica și administra contul propriu.
   - Există roluri: `ADMIN`, `USER`, `RESTAURANT_OWNER`.
   - Administrarea rolurilor și a drepturilor se asigură la nivel de aplicație.

2. **Gestionează restaurante**
   - Utilizator cu rol `RESTAURANT_OWNER` poate adăuga, edita și sterge restaurantele pe care le deține.
   - Fiecare restaurant are detalii: denumire (unic), adresă, telefon, site, program.

3. **Gestionare categorii de restaurante**
   - Restaurantele pot avea una sau mai multe categorii (ex: românesc, italian, fast food).
   - Categorii pot fi administrate independent.

4. **Meniu și articole de meniu**
   - Fiecare restaurant are unul sau mai multe meniuri (ex: Meniu Mâncare, Meniu Băuturi).
   - Fiecare meniu are articole (ex: Ciorbă de burtă, Sarmale), cu denumire, preț, descriere.

5. **Recenzii și feedback**
   - Utilizatorii pot lăsa recenzii restaurantelor cu notă (1-5 stele) și comentarii.
   - Fiecare recenzie poate primi răspuns din partea unui reprezentant al restaurantului.
   - Utilizatorii pot trimite feedback general privind experiența lor în aplicație (feedback-uri anonime sau autentificate).

6. **Căutare și filtrare**
   - Utilizatorii pot căuta restaurante după nume, categorie, locație etc.
   - Posibilitate filtrare restaurante după rating, specific culinar (categorie) etc.

7. **Administrare**
   - Administratorii au acces la gestionarea utilizatorilor, restaurantelor, categoriilor, feedback-urilor.

---

## Alte detalii

- Structura DB respectă relațiile din poza de mai sus.
- Orice modificare a ERD-ului necesită actualizarea diagrama și a cerințelor de mai sus.
