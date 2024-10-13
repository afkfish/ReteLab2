package hu.bme.aut.retelab2.repository;

import hu.bme.aut.retelab2.SecretGenerator;
import hu.bme.aut.retelab2.domain.Ad;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class AdRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public String save(Ad ad) {
        ad.creationDate = new Date();
        String secret = SecretGenerator.generate();
        ad.secret = secret;
        em.merge(ad);
        return secret;
    }

    public List<Ad> findAll() { return em.createQuery("SELECT a FROM Ad a", Ad.class).getResultList().stream().peek(ad -> ad.secret = null).collect(Collectors.toList()); }

    public Ad findById(long id) {
        Ad temp = em.find(Ad.class, id);
        if (Objects.isNull(temp)) {
            return null;
        }
        temp.secret = null;
        return temp;
    }

    public List<Ad> findByPriceRange(int min, int max) {
        return em.createQuery("SELECT a FROM Ad a WHERE a.price >= :min AND a.price <= :max", Ad.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList().stream().peek(ad -> ad.secret = null).collect(Collectors.toList());
    }

    public List<Ad> findByTag(String tag) {
        return em.createQuery("SELECT a FROM Ad a WHERE :tag MEMBER OF a.tags", Ad.class)
                .setParameter("tag", tag)
                .getResultList().stream().peek(ad -> ad.secret = null).collect(Collectors.toList());
    }

    @Scheduled(fixedDelay = 6000)
    @Transactional
    public void deleteExpiredAds() {
        List<Ad> ads = em.createQuery("SELECT a FROM Ad a", Ad.class).getResultList();
        LocalDateTime now = LocalDateTime.now();
        for (Ad ad : ads) {
            if (now.isAfter(ad.expirationDate)) {
                em.remove(ad);
            }
        }
    }

    @Transactional
    public Ad editAd(Ad ad) {
        Ad oldAd = em.find(Ad.class, ad.getId());
        if (ad.secret != null && ad.secret.equals(oldAd.secret)) {
            return em.merge(ad);
        }
        return null;
    }

    @Transactional
    public void deleteById(long id) {
        Ad ad = findById(id);
        em.remove(ad);
    }
}
