package springmockproject.busticketapp.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springmockproject.busticketapp.entity.User;
import springmockproject.busticketapp.entity.Wallet;
import springmockproject.busticketapp.exception.BookingException;
import springmockproject.busticketapp.repository.UserRepository;
import springmockproject.busticketapp.repository.WalletRepository;

@Service
public class WalletService {

    private final WalletRepository walletRepo;
    private final UserRepository userRepo;

    public WalletService(WalletRepository walletRepo, UserRepository userRepo) {
        this.walletRepo = walletRepo;
        this.userRepo = userRepo;
    }

    public Wallet getWalletByUsername(String username) {
        User user = userRepo.getUserById(username);
        if (user.getRole() != 0)
            return null;

        return walletRepo.getByUser(user);
    }

    public Wallet getWalletById(long id) {
        return walletRepo.findById(id);
    }


    @Transactional(rollbackFor = Exception.class)
    public void addBalance(long id, double amount) {
        Wallet wallet = walletRepo.findById(id);
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepo.save(wallet);
    }

    @Transactional(rollbackFor = BookingException.class)
    public void deduceBalance(long id, double amount) throws BookingException {
        Wallet wallet = walletRepo.findById(id);

        if (wallet.getBalance() < amount) {
            throw new
                    BookingException("CustomerException-Not enough money to complete this payment");
        } else {
            wallet.setBalance(wallet.getBalance() - amount);
            walletRepo.save(wallet);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBalance(Wallet wallet, double amount) {
        this.addBalance(wallet.getId(), amount);
    }
}
