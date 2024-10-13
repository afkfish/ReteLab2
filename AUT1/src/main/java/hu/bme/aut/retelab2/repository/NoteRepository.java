package hu.bme.aut.retelab2.repository;

import hu.bme.aut.retelab2.domain.Note;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class NoteRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Note save(Note feedback) {
        return em.merge(feedback);
    }

    public List<Note> findAll() {
        return em.createQuery("SELECT n FROM Note n", Note.class).getResultList();
    }

    public Note findById(long id) {
        return em.find(Note.class, id);
    }

    public List<Note> findByKeyword(String keyword) {
        return em.createQuery("SELECT n FROM Note n WHERE n.text LIKE ?1", Note.class).setParameter(1, '%' + keyword + '%').getResultList();
    }

    @Transactional
    public void deleteById(long id) {
        Note todo = findById(id);
        em.remove(todo);
    }
}
