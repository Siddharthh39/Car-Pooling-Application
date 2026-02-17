const state = {
    user: null
};

const configuredApiBase = (window.CAB_CONFIG && window.CAB_CONFIG.apiBaseUrl) ? window.CAB_CONFIG.apiBaseUrl.trim() : "";

const el = {
    alertBox: document.getElementById("alertBox"),
    sessionLabel: document.getElementById("sessionLabel"),
    currentUserId: document.getElementById("currentUserId"),
    name: document.getElementById("name"),
    email: document.getElementById("email"),
    source: document.getElementById("source"),
    destination: document.getElementById("destination"),
    seats: document.getElementById("seats"),
    fare: document.getElementById("fare"),
    searchSource: document.getElementById("searchSource"),
    searchDestination: document.getElementById("searchDestination"),
    ridesTableBody: document.getElementById("ridesTableBody"),
    bookingsTableBody: document.getElementById("bookingsTableBody")
};

document.getElementById("registerBtn").addEventListener("click", registerUser);
document.getElementById("loginBtn").addEventListener("click", loginUser);
document.getElementById("publishBtn").addEventListener("click", publishRide);
document.getElementById("refreshRidesBtn").addEventListener("click", fetchRides);
document.getElementById("searchBtn").addEventListener("click", fetchRides);
document.getElementById("refreshBookingsBtn").addEventListener("click", fetchBookings);

fetchRides();

function getApiBase() {
    if (!configuredApiBase) {
        throw new Error("API Base URL not configured");
    }
    return configuredApiBase;
}

async function request(path, options = {}) {
    const base = getApiBase();
    const response = await fetch(base + path, {
        headers: {"Content-Type": "application/json"},
        ...options
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : {};

    if (!response.ok) {
        throw new Error(data.error || "Request failed");
    }

    return data;
}

function notify(message, type = "success") {
    el.alertBox.className = `alert alert-${type}`;
    el.alertBox.textContent = message;
}

function updateSessionUi() {
    if (state.user) {
        el.sessionLabel.textContent = `${state.user.name} (${state.user.email})`;
        el.currentUserId.textContent = state.user.id;
    } else {
        el.sessionLabel.textContent = "Not logged in";
        el.currentUserId.textContent = "-";
    }
}

async function registerUser() {
    try {
        const name = el.name.value.trim();
        const email = el.email.value.trim();
        if (!name || !email) {
            throw new Error("Name and email are required");
        }

        const data = await request("/users/register", {
            method: "POST",
            body: JSON.stringify({name, email})
        });

        state.user = data;
        updateSessionUi();
        notify(`Logged in as ${data.name}`);
        await fetchBookings();
    } catch (error) {
        notify(error.message, "danger");
    }
}

async function loginUser() {
    try {
        const email = el.email.value.trim();
        if (!email) {
            throw new Error("Email is required");
        }

        const user = await request(`/users/login?email=${encodeURIComponent(email)}`);
        state.user = user;
        updateSessionUi();
        notify(`Welcome back ${user.name}`);
        await fetchBookings();
    } catch (error) {
        notify(error.message, "danger");
    }
}

async function publishRide() {
    try {
        if (!state.user) {
            throw new Error("Login first");
        }

        const payload = {
            ownerId: state.user.id,
            source: el.source.value.trim(),
            destination: el.destination.value.trim(),
            seats: Number(el.seats.value),
            farePerSeat: Number(el.fare.value)
        };

        if (!payload.source || !payload.destination || payload.seats <= 0 || payload.farePerSeat <= 0) {
            throw new Error("Fill all ride fields with valid values");
        }

        await request("/rides", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        notify("Ride published successfully");
        await fetchRides();
    } catch (error) {
        notify(error.message, "danger");
    }
}

async function fetchRides() {
    try {
        let path = "/rides";
        const source = el.searchSource.value.trim();
        const destination = el.searchDestination.value.trim();
        if (source && destination) {
            path += `?source=${encodeURIComponent(source)}&destination=${encodeURIComponent(destination)}`;
        }

        const rides = await request(path);
        renderRides(rides);
    } catch (error) {
        notify(error.message, "warning");
    }
}

function renderRides(rides) {
    if (!rides.length) {
        el.ridesTableBody.innerHTML = "<tr><td colspan='5' class='text-secondary text-center py-3'>No rides found</td></tr>";
        return;
    }

    el.ridesTableBody.innerHTML = rides.map((ride) => `
        <tr>
            <td>${ride.id}</td>
            <td>${ride.source} → ${ride.destination}</td>
            <td>${ride.seats}</td>
            <td>₹${ride.farePerSeat}</td>
            <td><button class="btn btn-sm btn-primary" onclick="bookRide(${ride.id})">Book</button></td>
        </tr>
    `).join("");
}

async function bookRide(rideId) {
    try {
        if (!state.user) {
            throw new Error("Login first");
        }

        const seats = Number(prompt("How many seats do you want to book?", "1"));
        if (!seats || seats <= 0) {
            return;
        }

        await request("/bookings", {
            method: "POST",
            body: JSON.stringify({
                rideId,
                userId: state.user.id,
                seats
            })
        });

        notify("Ride booked successfully");
        await Promise.all([fetchRides(), fetchBookings()]);
    } catch (error) {
        notify(error.message, "danger");
    }
}

async function fetchBookings() {
    if (!state.user) {
        el.bookingsTableBody.innerHTML = "<tr><td colspan='5' class='text-secondary text-center py-3'>Login to see bookings</td></tr>";
        return;
    }

    try {
        const bookings = await request(`/bookings/${state.user.id}`);
        renderBookings(bookings);
    } catch (error) {
        notify(error.message, "warning");
    }
}

function renderBookings(bookings) {
    if (!bookings.length) {
        el.bookingsTableBody.innerHTML = "<tr><td colspan='5' class='text-secondary text-center py-3'>No bookings found</td></tr>";
        return;
    }

    el.bookingsTableBody.innerHTML = bookings.map((booking) => `
        <tr>
            <td>${booking.id}</td>
            <td>${booking.rideId}</td>
            <td>${booking.seatsBooked}</td>
            <td>₹${booking.totalFare}</td>
            <td><button class="btn btn-sm btn-outline-danger" onclick="cancelBooking(${booking.id})">Cancel</button></td>
        </tr>
    `).join("");
}

async function cancelBooking(bookingId) {
    try {
        if (!state.user) {
            throw new Error("Login first");
        }

        await request(`/bookings/${bookingId}?userId=${state.user.id}`, {
            method: "DELETE"
        });

        notify("Booking cancelled");
        await Promise.all([fetchRides(), fetchBookings()]);
    } catch (error) {
        notify(error.message, "danger");
    }
}

window.bookRide = bookRide;
window.cancelBooking = cancelBooking;
updateSessionUi();
