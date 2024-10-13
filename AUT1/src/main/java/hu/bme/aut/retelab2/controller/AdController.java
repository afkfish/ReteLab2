package hu.bme.aut.retelab2.controller;

import hu.bme.aut.retelab2.domain.Ad;
import hu.bme.aut.retelab2.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ads")
public class AdController {

    @Autowired
    private AdRepository adRepository;

    @PostMapping
    public String createAd(@RequestBody Ad ad) {
        return adRepository.save(ad);
    }

    @GetMapping
    public List<Ad> getAds(@RequestParam(defaultValue = "0") int min, @RequestParam(defaultValue = "10000000") int max) {
        return adRepository.findByPriceRange(min, max);
    }

    @PutMapping
    public ResponseEntity<Ad> editAd(@RequestBody Ad ad) {
        Ad editedAd = adRepository.editAd(ad);
        if (editedAd == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN) ;
        }
        return ResponseEntity.ok(editedAd);
    }

    @GetMapping("/{tag}")
    public List<Ad> getAdsByTag(@PathVariable String tag) {
        return adRepository.findByTag(tag);
    }
}
